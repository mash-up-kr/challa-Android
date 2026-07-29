package com.happyhouse.challa.presentation.gallery

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryIntent
import com.happyhouse.challa.presentation.gallery.contract.GalleryMemberUiModel
import com.happyhouse.challa.presentation.gallery.contract.GallerySideEffect
import com.happyhouse.challa.presentation.gallery.contract.GalleryState
import com.happyhouse.challa.presentation.gallery.contract.GalleryState.PhotoInfo
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
            GalleryIntent.PrintCountdownClick -> handlePrintCountdownClick()
        }
    }

    private fun handlePhotosLoad() {
        viewModelScope.launch {
            updateState { copy(photoInfo = PhotoInfo.Loading) }
            runCatching {
                loadMockPhotoInfo()
            }.onSuccess { photoInfo ->
                updateState {
                    copy(
                        roomName = MOCK_ROOM_NAME,
                        members = loadMockMembers(),
                        photoInfo = photoInfo,
                    )
                }
            }.onFailure { throwable ->
                if (throwable is CancellationException) throw throwable
                Timber.e(throwable, "갤러리 사진을 불러오지 못했습니다")
                updateState { copy(photoInfo = PhotoInfo.Error) }
            }
        }
    }

    private fun handlePhotoClick(photoId: Long) {
        viewModelScope.launch {
            sendEffect(GallerySideEffect.NavigateToPhotoDetail(photoId))
        }
    }

    private fun handlePrintCountdownClick() {
        viewModelScope.launch {
            sendEffect(GallerySideEffect.PrintWaiting)
        }
    }

    // TODO: 실제 API 연동 전까지 쓰는 mock 데이터. 인화 여부도 서버 응답으로 판단하도록 교체할 것.
    private suspend fun loadMockPhotoInfo(): PhotoInfo {
        delay(MOCK_LOAD_DELAY_MS) // TODO: 로딩 상태 확인용으로 실제 API 붙으면 제거하기
        return PhotoInfo.Waiting(
            slotCount = MOCK_PHOTO_COUNT,
            remainingSeconds = MOCK_REMAINING_SECONDS,
        )
    }

    // TODO: 실제 API 연동 전까지 쓰는 mock 참여자
    private fun loadMockMembers(): ImmutableList<GalleryMemberUiModel> =
        (0 until MOCK_MEMBER_COUNT)
            .map { index ->
                GalleryMemberUiModel(
                    id = index.toLong(),
                    profileImageUrl = "https://picsum.photos/seed/${roomId}_member_$index/60/60",
                )
            }.toPersistentList()

    @AssistedFactory
    interface Factory {
        fun create(roomId: Long): GalleryViewModel
    }

    companion object {
        private const val MOCK_PHOTO_COUNT = 24
        private const val MOCK_MEMBER_COUNT = 6
        private const val MOCK_REMAINING_SECONDS = 10_798L
        private const val MOCK_LOAD_DELAY_MS = 300L
        private const val MOCK_ROOM_NAME = "다낭 4박5일"
    }
}
