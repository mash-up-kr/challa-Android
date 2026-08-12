package com.happyhouse.challa.presentation.photodetail

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.model.RoomDetail
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.domain.repository.PhotoRepository
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.causeOrNull
import com.happyhouse.challa.presentation.base.BaseViewModel
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

    // TODO: 반응 API 연동 전까지 로컬에서 발급하는 반응 id. 좌표 seed로 쓰이므로 반응마다 고유해야 한다.
    private var nextReactionId = 0L

    init {
        onIntent(PhotoDetailIntent.PhotosLoad)
    }

    override fun onIntent(intent: PhotoDetailIntent) {
        when (intent) {
            PhotoDetailIntent.PhotosLoad -> handlePhotosLoad()
            is PhotoDetailIntent.PhotoSave -> handlePhotoSave(intent.photo)
            is PhotoDetailIntent.ReactionClick -> handleReactionClick(intent.photo, intent.emoji)
            is PhotoDetailIntent.MessageChange -> handleMessageChange(intent.message)
            is PhotoDetailIntent.MessageSend -> handleMessageSend(intent.photo)
        }
    }

    private fun handlePhotosLoad() {
        // 재시도를 연타하면 조회가 겹쳐 나중에 끝난 응답이 이긴다.
        loadJob?.cancel()

        loadJob =
            viewModelScope.launch {
                updateState { copy(photoInfo = PhotoInfo.Loading) }

                val roomDeferred = async { roomRepository.getRoom(roomId) }
                val photosResult = photoRepository.getPhotos(roomId)
                val roomResult = roomDeferred.await()

                if (photosResult !is ChallaResult.Success) {
                    Timber.e(photosResult.causeOrNull(), "사진을 불러오지 못했습니다. photos=$photosResult")
                    updateState { copy(photoInfo = PhotoInfo.Error) }
                    sendEffect(PhotoDetailSideEffect.PhotosLoadFailed)
                    return@launch
                }

                val room = roomResult.roomOrNull()
                val photos = photosResult.data.toPhotoDetailUiModels()

                // 사진과 제목을 함께 반영해, 제목이 사진보다 늦게 나타나지 않게 한다.
                updateState {
                    copy(
                        roomName = room?.title ?: roomName,
                        photoInfo = if (photos.isEmpty()) PhotoInfo.Empty else PhotoInfo.Loaded(photos),
                    )
                }
            }
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
                    Timber.w(causeOrNull(), "방 정보를 불러오지 못했습니다. room=$this")
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
            @Assisted("initialPhotoId") initialPhotoId: Long,
        ): PhotoDetailViewModel
    }
}
