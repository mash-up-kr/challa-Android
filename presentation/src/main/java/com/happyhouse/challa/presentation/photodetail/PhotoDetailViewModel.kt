package com.happyhouse.challa.presentation.photodetail

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.repository.PhotoRepository
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailIntent
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailSideEffect
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState.PhotoInfo
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailUiModel
import com.happyhouse.challa.presentation.photodetail.contract.PhotoReactionUiModel
import com.happyhouse.challa.presentation.photodetail.contract.ReactionEmoji
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = PhotoDetailViewModel.Factory::class)
class PhotoDetailViewModel @AssistedInject constructor(
    @Assisted("roomId") private val roomId: Long,
    @Assisted("initialPhotoId") private val initialPhotoId: Long,
    private val photoRepository: PhotoRepository,
) : BaseViewModel<PhotoDetailState, PhotoDetailIntent, PhotoDetailSideEffect>(
        initialState = PhotoDetailState(roomId = roomId, initialPhotoId = initialPhotoId),
    ) {
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
        viewModelScope.launch {
            updateState { copy(photoInfo = PhotoInfo.Loading) }
            runCatching {
                loadMockPhotos()
            }.onSuccess { photos ->
                updateState {
                    copy(
                        roomName = MOCK_ROOM_NAME,
                        photoInfo = if (photos.isEmpty()) PhotoInfo.Empty else PhotoInfo.Loaded(photos),
                    )
                }
            }.onFailure { throwable ->
                if (throwable is CancellationException) throw throwable
                Timber.e(throwable, "사진 상세 정보를 불러오지 못했습니다")
                updateState { copy(photoInfo = PhotoInfo.Error) }
                sendEffect(PhotoDetailSideEffect.PhotosLoadFailed)
            }
        }
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

    // TODO: 실제 API 연동 전까지 쓰는 mock 데이터
    private suspend fun loadMockPhotos(): ImmutableList<PhotoDetailUiModel> {
        delay(MOCK_LOAD_DELAY_MS) // TODO: 로딩 상태 확인용으로 실제 API 붙으면 제거하기
        return (0 until MOCK_PHOTO_COUNT)
            .map { index ->
                PhotoDetailUiModel(
                    id = index.toLong(),
                    imageUrl = "https://picsum.photos/seed/${roomId}_$index/600/800",
                    photographer = MOCK_PHOTOGRAPHER,
                    capturedDate = MOCK_CAPTURED_DATE,
                )
            }.toPersistentList()
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("roomId") roomId: Long,
            @Assisted("initialPhotoId") initialPhotoId: Long,
        ): PhotoDetailViewModel
    }

    companion object {
        private const val MOCK_PHOTO_COUNT = 24
        private const val MOCK_LOAD_DELAY_MS = 300L
        private const val MOCK_ROOM_NAME = "길고양이를찍으러가자"
        private const val MOCK_PHOTOGRAPHER = "이주연"
        private const val MOCK_CAPTURED_DATE = "2026. 7. 16. 14:34"
    }
}
