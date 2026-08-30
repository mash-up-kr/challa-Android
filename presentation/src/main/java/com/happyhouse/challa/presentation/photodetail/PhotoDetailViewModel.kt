package com.happyhouse.challa.presentation.photodetail

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.event.RoomEvent
import com.happyhouse.challa.domain.model.PhotoPage
import com.happyhouse.challa.domain.repository.PhotoRepository
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.result.causeOrNull
import com.happyhouse.challa.domain.result.onFailure
import com.happyhouse.challa.domain.result.onSuccess
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.navigation.PhotoDetailArgs
import com.happyhouse.challa.presentation.navigation.toPhotos
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailIntent
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailSideEffect
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState.PhotoInfo
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailUiModel
import com.happyhouse.challa.presentation.photodetail.contract.PhotoReactionUiModel
import com.happyhouse.challa.presentation.photodetail.contract.ReactionEmoji
import com.happyhouse.challa.presentation.photodetail.util.toPhotoDetailUiModels
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = PhotoDetailViewModel.Factory::class)
class PhotoDetailViewModel @AssistedInject constructor(
    @Assisted("roomId") private val roomId: Long,
    @Assisted args: PhotoDetailArgs,
    private val photoRepository: PhotoRepository,
    private val roomRepository: RoomRepository,
) : BaseViewModel<PhotoDetailState, PhotoDetailIntent, PhotoDetailSideEffect>(
        initialState = initialPhotoDetailState(args),
    ) {
    private var appendJob: Job? = null

    private val loadedPhotos = args.photos.toPhotos().toMutableList()
    private val loadedPhotoIds = loadedPhotos.mapTo(mutableSetOf()) { photo -> photo.id }
    private var nextPhotoPage = args.nextPhotoPage
    private var hasNextPhotoPage = args.hasNextPhotoPage

    // TODO: 반응 API 연동 전까지 로컬에서 발급하는 반응 id. 좌표 seed로 쓰이므로 반응마다 고유해야 한다.
    private var nextReactionId = 0L

    init {
        observeRoomEvents()
    }

    /** 사진 상세가 열려 있는 동안 방 이름이 바뀌면 제목만 갈아끼운다. 사진을 다시 받을 이유는 없다. */
    private fun observeRoomEvents() {
        viewModelScope.launch {
            roomRepository.roomEventFlow
                .filterIsInstance<RoomEvent.TitleUpdate>()
                .filter { it.roomId == roomId }
                .collect { event -> updateState { copy(roomName = event.title) } }
        }
    }

    override fun onIntent(intent: PhotoDetailIntent) {
        when (intent) {
            PhotoDetailIntent.PhotosLoadMore -> handlePhotosLoadMore()
            is PhotoDetailIntent.PhotoSave -> handlePhotoSave(intent.photo)
            is PhotoDetailIntent.ReactionClick -> handleReactionClick(intent.photo, intent.emoji)
            is PhotoDetailIntent.MessageChange -> handleMessageChange(intent.message)
            is PhotoDetailIntent.MessageSend -> handleMessageSend(intent.photo)
        }
    }

    private fun handlePhotosLoadMore() {
        if (!hasNextPhotoPage) return
        if (appendJob?.isActive == true) return

        val requestedPage = nextPhotoPage
        appendJob =
            viewModelScope.launch {
                photoRepository
                    .getPhotos(roomId, requestedPage)
                    .onSuccess { photoPage ->
                        appendPhotoPage(photoPage)
                        val photos = loadedPhotos.toPhotoDetailUiModels()

                        updateState {
                            // 이어 받는 동안 다시 조회가 돌면 로딩/에러로 바뀌어 있을 수 있다. 그때는 덮어쓰지 않는다.
                            val loaded =
                                photoInfo as? PhotoInfo.Loaded
                                    ?: run {
                                        Timber.w("사진 목록이 열려 있지 않아 이어 받은 페이지를 반영하지 않습니다: $photoInfo")
                                        return@updateState this
                                    }
                            copy(photoInfo = loaded.copy(photos = photos))
                        }
                    }.onFailure { failure ->
                        Timber.e(
                            failure.causeOrNull(),
                            "다음 사진 페이지를 불러오지 못했습니다. roomId=$roomId, page=$requestedPage",
                        )
                        sendEffect(PhotoDetailSideEffect.PhotosLoadMoreFailed)
                    }
            }
    }

    /** 페이지를 받는 사이에 사진이 늘면 같은 사진이 두 페이지에 걸쳐 오고, 페이저의 key가 겹쳐 깨진다. */
    private fun appendPhotoPage(photoPage: PhotoPage) {
        loadedPhotos += photoPage.photos.filter { photo -> loadedPhotoIds.add(photo.id) }
        hasNextPhotoPage = photoPage.hasNext
        nextPhotoPage++
    }

    private fun handlePhotoSave(photo: PhotoDetailUiModel) {
        if (currentState.isSaving) return

        updateState { copy(isSaving = true) }
        viewModelScope.launch {
            try {
                photoRepository
                    .savePhoto(photo.imageUrl)
                    .onSuccess { sendEffect(PhotoDetailSideEffect.SaveSucceeded) }
                    .onFailure { throwable ->
                        Timber.e(throwable, "사진 저장 실패")
                        sendEffect(PhotoDetailSideEffect.SaveFailed)
                    }
            } finally {
                updateState { copy(isSaving = false) }
            }
        }
    }

    /**
     * TODO: 반응 API 스펙 확정 전까지 로컬 state에만 쌓는다. (이슈 #62)
     * TODO: 같은 사람이 여러 번 남길 수 있는지 / 취소 가능한지 기획 미확정. 현재는 누를 때마다 계속 추가된다.
     */
    private fun handleReactionClick(
        photo: PhotoDetailUiModel,
        emoji: ReactionEmoji,
    ) {
        val loaded = currentState.photoInfo as? PhotoInfo.Loaded
        if (loaded == null) {
            Timber.w("사진이 로드되지 않아 반응을 남기지 못했습니다: photoId=${photo.id}")
            viewModelScope.launch { sendEffect(PhotoDetailSideEffect.ReactionSendFailed) }
            return
        }

        val reaction = PhotoReactionUiModel(id = nextReactionId++, emoji = emoji)
        val updated = (loaded.reactionsOf(photo.id) + reaction).toPersistentList()
        val reactions = (loaded.reactions + (photo.id to updated)).toPersistentMap()
        updateState { copy(photoInfo = loaded.copy(reactions = reactions)) }
    }

    private fun handleMessageChange(message: String) {
        updateState { copy(messageInput = message) }
    }

    /**
     * 보낸 뒤 입력만 비우고 키보드는 유지한다.
     * TODO: 메시지 API 스펙 확정 전까지 전송 결과를 성공으로 가정한다. (이슈 #62)
     *   실패 경로가 생기면 MessageSendFailed SideEffect를 다시 추가할 것.
     */
    private fun handleMessageSend(photo: PhotoDetailUiModel) {
        val message = currentState.messageInput.trim()
        if (message.isEmpty() || currentState.isSendingMessage) {
            Timber.w("보낼 수 없는 메시지라 전송하지 않았습니다: photoId=${photo.id}")
            return
        }

        updateState { copy(isSendingMessage = true) }
        viewModelScope.launch {
            try {
                // 메시지 본문은 개인정보라 로그에 남기지 않는다.
                Timber.d("사진 메시지 전송: photoId=${photo.id}, length=${message.length}")
                updateState { copy(messageInput = "") }
            } finally {
                updateState { copy(isSendingMessage = false) }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("roomId") roomId: Long,
            args: PhotoDetailArgs,
        ): PhotoDetailViewModel
    }
}

private fun initialPhotoDetailState(args: PhotoDetailArgs): PhotoDetailState {
    val photos = args.photos.toPhotos().toPhotoDetailUiModels()

    return PhotoDetailState(
        initialPhotoIndex = args.initialPhotoIndex,
        roomName = args.roomName,
        photoInfo = if (photos.isEmpty()) PhotoInfo.Empty else PhotoInfo.Loaded(photos),
    )
}
