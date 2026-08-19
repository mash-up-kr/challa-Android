package com.happyhouse.challa.presentation.photodetail

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.model.Photo
import com.happyhouse.challa.domain.model.PhotoPage
import com.happyhouse.challa.domain.model.RoomDetail
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.domain.repository.PhotoRepository
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.causeOrNull
import com.happyhouse.challa.domain.result.onFailure
import com.happyhouse.challa.domain.result.onSuccess
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.photodetail.contract.MAX_REACTION_COUNT
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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = PhotoDetailViewModel.Factory::class)
class PhotoDetailViewModel @AssistedInject constructor(
    @Assisted("roomId") private val roomId: Long,
    @Assisted("initialPhotoId") private val initialPhotoId: Long,
    private val roomRepository: RoomRepository,
    private val photoRepository: PhotoRepository,
) : BaseViewModel<PhotoDetailState, PhotoDetailIntent, PhotoDetailSideEffect>(
        initialState = PhotoDetailState(roomId = roomId, initialPhotoId = initialPhotoId),
    ) {
    private var loadJob: Job? = null
    private var appendJob: Job? = null

    /** 지금까지 받아둔 사진 */
    private val loadedPhotos = mutableListOf<Photo>()
    private val loadedPhotoIds = mutableSetOf<Long>()
    private var nextPhotoPage = FIRST_PHOTO_PAGE
    private var hasNextPhotoPage = false

    // TODO: 반응 API 연동 전까지 로컬에서 발급하는 반응 id. 배치 seed로 쓰이므로 반응마다 고유해야 한다.
    private var nextReactionId = 0L

    init {
        onIntent(PhotoDetailIntent.PhotosLoad)
    }

    override fun onIntent(intent: PhotoDetailIntent) {
        when (intent) {
            PhotoDetailIntent.PhotosLoad -> handlePhotosLoad()
            PhotoDetailIntent.PhotosLoadMore -> handlePhotosLoadMore()
            is PhotoDetailIntent.PhotoSave -> handlePhotoSave(intent.photo)
            is PhotoDetailIntent.ReactionClick -> handleReactionClick(intent.photo, intent.emoji)
            is PhotoDetailIntent.MessageChange -> handleMessageChange(intent.message)
            is PhotoDetailIntent.MessageSend -> handleMessageSend(intent.photo)
        }
    }

    private fun handlePhotosLoad() {
        // 재시도를 연타하면 조회가 겹쳐 나중에 끝난 응답이 이긴다.
        loadJob?.cancel()
        // 이전 목록에 이어 붙이려던 페이지가 뒤늦게 도착해 섞이지 않게 한다.
        appendJob?.cancel()
        resetPhotoPaging()

        loadJob =
            viewModelScope.launch {
                updateState { copy(photoInfo = PhotoInfo.Loading) }

                val roomDeferred = async { roomRepository.getRoom(roomId) }
                val photosResult = loadPagesUntilInitialPhoto()
                val roomResult = roomDeferred.await()

                photosResult
                    .onSuccess {
                        val room = roomResult.roomOrNull()
                        val photos = loadedPhotos.toPhotoDetailUiModels()

                        // 사진과 제목을 함께 반영해, 제목이 사진보다 늦게 나타나지 않게 한다.
                        updateState {
                            copy(
                                roomName = room?.title ?: roomName,
                                photoInfo = if (photos.isEmpty()) PhotoInfo.Empty else PhotoInfo.Loaded(photos),
                            )
                        }
                    }.onFailure { failure ->
                        Timber.e(failure.causeOrNull(), "사진을 불러오지 못했습니다. roomId=$roomId")
                        updateState { copy(photoInfo = PhotoInfo.Error) }
                        sendEffect(PhotoDetailSideEffect.PhotosLoadFailed)
                    }
            }
    }

    /**
     * 진입한 사진이 나올 때까지 첫 페이지부터 이어 받는다.
     *
     * 갤러리에서 뒤쪽 사진을 눌러 들어오면 그 사진이 첫 페이지에 없어 페이저를 그 자리에서 열 수 없다.
     * 서버가 [PhotoPage.hasNext] 를 계속 true로 내려주면 끝나지 않으므로, 상한에 걸리면 받은 만큼이라도 그린다.
     */
    private suspend fun loadPagesUntilInitialPhoto(): ChallaResult<Unit> {
        repeat(MAX_INITIAL_PHOTO_PAGE_COUNT) {
            val pageResult = photoRepository.getPhotos(roomId, nextPhotoPage)

            when (pageResult) {
                is ChallaResult.Success -> appendPhotoPage(pageResult.data)
                is ChallaResult.Failure -> return pageResult
            }

            if (initialPhotoId in loadedPhotoIds || !hasNextPhotoPage) return ChallaResult.Success(Unit)
        }

        Timber.w(
            "진입한 사진을 ${MAX_INITIAL_PHOTO_PAGE_COUNT}페이지까지 찾지 못해 이어 받기를 멈춥니다. " +
                "roomId=$roomId, photoId=$initialPhotoId",
        )
        return ChallaResult.Success(Unit)
    }

    /** 넘기는 중에 올라오는 신호라 화면을 로딩으로 되돌리지 않고, 받아둔 사진 뒤에만 덧붙인다. */
    private fun handlePhotosLoadMore() {
        if (!hasNextPhotoPage) return
        if (appendJob?.isActive == true || loadJob?.isActive == true) return

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

    private fun resetPhotoPaging() {
        loadedPhotos.clear()
        loadedPhotoIds.clear()
        nextPhotoPage = FIRST_PHOTO_PAGE
        hasNextPhotoPage = false
    }

    /** 페이지를 받는 사이에 사진이 늘면 같은 사진이 두 페이지에 걸쳐 오고, 페이저의 key가 겹쳐 깨진다. */
    private fun appendPhotoPage(photoPage: PhotoPage) {
        loadedPhotos += photoPage.photos.filter { photo -> loadedPhotoIds.add(photo.id) }
        hasNextPhotoPage = photoPage.hasNext
        nextPhotoPage++
    }

    /**
     * 방 이름은 사진 옆의 부가 정보라, 조회에 실패해도 사진은 그대로 그리고 이미 떠 있는 제목을 유지한다.
     *
     * 상세는 인화가 끝난 방에서만 열리는 것을 전제로 원본을 그린다. 전제가 깨지면 미공개 사진이
     * 원본으로 노출되므로, 진입 경로가 늘었을 때 알아차릴 수 있게 로그를 남긴다.
     * TODO: 인화 전 방에서도 상세를 열 수 있게 되면 블러 여부를 기획과 맞춰 화면에 반영할 것.
     */
    private fun ChallaResult<RoomDetail>.roomOrNull(): RoomDetail? {
        val room =
            when (this) {
                is ChallaResult.Success -> data
                is ChallaResult.Failure -> {
                    Timber.w(causeOrNull(), "방 정보를 불러오지 못했습니다. roomId=$roomId")
                    return null
                }
            }

        if (room.status != RoomStatus.PHOTO_PRINT_COMPLETED) {
            Timber.w("인화가 끝나지 않은 방의 사진을 상세에서 원본으로 그리고 있습니다. roomId=$roomId, status=${room.status}")
        }

        return room
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
     * 이미 남긴 이모지를 다시 누르면 취소하고, 아니면 새로 남긴다.
     * 같은 이모지는 한 번만 붙으므로 한 사진에 서로 다른 이모지가 최대 [MAX_REACTION_COUNT]개까지 붙는다.
     *
     * TODO: 반응 삭제 API가 없어 등록·취소 모두 로컬 state에만 반영한다. API가 나오면 연동할 것. (이슈 #110)
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

        val current = loaded.reactionsOf(photo.id)
        val left = current.firstOrNull { reaction -> reaction.emoji == emoji }

        val updated =
            when {
                left != null -> current - left

                current.size >= MAX_REACTION_COUNT -> {
                    viewModelScope.launch { sendEffect(PhotoDetailSideEffect.ReactionLimitExceeded) }
                    return
                }

                else -> current + PhotoReactionUiModel(id = nextReactionId++, emoji = emoji)
            }

        val reactions = (loaded.reactions + (photo.id to updated.toPersistentList())).toPersistentMap()
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
            @Assisted("initialPhotoId") initialPhotoId: Long,
        ): PhotoDetailViewModel
    }

    companion object {
        private const val FIRST_PHOTO_PAGE = 0

        /** 진입한 사진을 찾느라 이어 받을 페이지 상한 */
        private const val MAX_INITIAL_PHOTO_PAGE_COUNT = 10
    }
}
