package com.happyhouse.challa.presentation.gallery

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryIntent
import com.happyhouse.challa.presentation.gallery.contract.GalleryPhotoUiModel
import com.happyhouse.challa.presentation.gallery.contract.GallerySideEffect
import com.happyhouse.challa.presentation.gallery.contract.GalleryState
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

@HiltViewModel(assistedFactory = GalleryViewModel.Factory::class)
class GalleryViewModel @AssistedInject constructor(
    @Assisted private val roomId: Long,
) : BaseViewModel<GalleryState, GalleryIntent, GallerySideEffect>(
        initialState = GalleryState(roomId = roomId),
    ) {
    init {
        onIntent(GalleryIntent.PhotosLoad)
    }

    override fun onIntent(intent: GalleryIntent) {
        when (intent) {
            GalleryIntent.PhotosLoad -> handlePhotosLoad()
            is GalleryIntent.PhotoClick -> handlePhotoClick(intent.photoId)
        }
    }

    private fun handlePhotosLoad() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, isError = false) }
            runCatching {
                loadMockPhotos()
            }.onSuccess { photos ->
                updateState {
                    copy(isLoading = false, isError = false, photos = photos, roomName = MOCK_ROOM_NAME)
                }
            }.onFailure { throwable ->
                if (throwable is CancellationException) throw throwable
                Timber.e(throwable)
                updateState { copy(isLoading = false, isError = true) }
            }
        }
    }

    private fun handlePhotoClick(photoId: Long) {
        viewModelScope.launch {
            sendEffect(GallerySideEffect.NavigateToPhotoDetail(photoId))
        }
    }

    // TODO: 실제 API 연동 전까지 쓰는 mock 데이터
    private suspend fun loadMockPhotos(): ImmutableList<GalleryPhotoUiModel> {
        delay(MOCK_LOAD_DELAY_MS) // TODO: 로딩 상태 확인용으로 실제 API 붙으면 제거하기
        return (0 until MOCK_PHOTO_COUNT)
            .map { index ->
                GalleryPhotoUiModel(
                    id = index.toLong(),
                    order = index + 1,
                    imageUrl = "https://picsum.photos/seed/${roomId}_$index/300/300",
                )
            }.toPersistentList()
    }

    @AssistedFactory
    interface Factory {
        fun create(roomId: Long): GalleryViewModel
    }

    companion object {
        private const val MOCK_PHOTO_COUNT = 24
        private const val MOCK_LOAD_DELAY_MS = 300L
        private const val MOCK_ROOM_NAME = "다낭 4박5일"
    }
}
