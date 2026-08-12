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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
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

                // 방 이름은 상단바에만 쓰이므로 사진과 함께 요청하되 사진 표시를 기다리게 하지 않는다.
                val roomDeferred = async { roomRepository.getRoom(roomId) }
                val photosResult = photoRepository.getPhotos(roomId)

                if (photosResult !is ChallaResult.Success) {
                    Timber.e(photosResult.causeOrNull(), "사진을 불러오지 못했습니다. photos=$photosResult")
                    roomDeferred.cancel()
                    updateState { copy(photoInfo = PhotoInfo.Error) }
                    sendEffect(PhotoDetailSideEffect.PhotosLoadFailed)
                    return@launch
                }

                val photos = photosResult.data.toPhotoDetailUiModels()
                updateState {
                    copy(photoInfo = if (photos.isEmpty()) PhotoInfo.Empty else PhotoInfo.Loaded(photos))
                }

                updateRoomWhenReady(roomDeferred)
            }
    }

    /**
     * 방 정보를 받는 대로 상단바 제목에 채운다.
     *
     * 방 정보는 사진 옆의 부가 정보라, 조회가 늦어져도 사진 표시를 붙잡지 않도록 사진을 그린 뒤 따로 기다린다.
     * 실패하면 사진과 이미 떠 있는 제목을 그대로 둔다. 재조회 실패로 멀쩡히 보이던 제목이 사라지지 않게 하기 위함이다.
     */
    private fun CoroutineScope.updateRoomWhenReady(roomDeferred: Deferred<ChallaResult<RoomDetail>>) {
        launch {
            val room =
                when (val roomResult = roomDeferred.await()) {
                    is ChallaResult.Success -> roomResult.data
                    is ChallaResult.Failure -> {
                        Timber.w(roomResult.causeOrNull(), "방 정보를 불러오지 못했습니다. room=$roomResult")
                        return@launch
                    }
                }

            warnIfPhotosNotPrinted(room)
            updateState { copy(roomName = room.title) }
        }
    }

    /**
     * 이 화면은 인화가 끝난 방에서만 열리는 것을 전제로 원본 사진을 그대로 그린다.
     * (인화 전 사진은 갤러리에서 앱이 블러 처리해 보여주고, 상세로는 들어갈 수 없다.)
     *
     * 전제가 깨지면 미공개 사진이 원본으로 노출되므로, 진입 경로가 늘었을 때 알아차릴 수 있게 남긴다.
     * TODO: 인화 전 방에서도 상세를 열 수 있게 되면 블러 여부를 기획과 맞춰 화면에 반영할 것.
     */
    private fun warnIfPhotosNotPrinted(room: RoomDetail) {
        if (room.status == RoomStatus.PHOTO_PRINT_COMPLETED) return

        Timber.w("인화가 끝나지 않은 방의 사진을 상세에서 원본으로 그리고 있습니다. roomId=$roomId, status=${room.status}")
    }

    private fun handlePhotoSave(photo: PhotoDetailUiModel) {
        if (currentState.isSaving) return

        val imageUrl = photo.imageUrl
        if (imageUrl == null) {
            // 화면에서 저장 버튼을 감추지만, 여기까지 들어오면 사용자에게 실패를 알린다.
            Timber.w("이미지 주소가 없어 저장하지 못했습니다: photoId=${photo.id}")
            viewModelScope.launch { sendEffect(PhotoDetailSideEffect.SaveFailed) }
            return
        }

        updateState { copy(isSaving = true) }
        viewModelScope.launch {
            try {
                photoRepository
                    .savePhoto(imageUrl)
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
