package com.happyhouse.challa.presentation.photodetail

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailIntent
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailSideEffect
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState.PhotoInfo
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailUiModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = PhotoDetailViewModel.Factory::class)
class PhotoDetailViewModel @AssistedInject constructor(
    @Assisted("roomId") private val roomId: Long,
    @Assisted("initialPhotoId") private val initialPhotoId: Long,
) : BaseViewModel<PhotoDetailState, PhotoDetailIntent, PhotoDetailSideEffect>(
        initialState = PhotoDetailState(roomId = roomId, initialPhotoId = initialPhotoId),
    ) {
    init {
        onIntent(PhotoDetailIntent.PhotosLoad)
    }

    override fun onIntent(intent: PhotoDetailIntent) {
        when (intent) {
            PhotoDetailIntent.PhotosLoad -> handlePhotosLoad()
            is PhotoDetailIntent.PhotoSave -> handlePhotoSave(intent.imageUrl)
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
            }
        }
    }

    private fun handlePhotoSave(imageUrl: String) {
        // TODO: 저장 중복 방지(isSaving 가드)는 후속 이슈에서 추가
        viewModelScope.launch {
            sendEffect(PhotoDetailSideEffect.SavePhotoToDevice(imageUrl))
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
        private const val MOCK_CAPTURED_DATE = "Oct 12 2026"
    }
}
