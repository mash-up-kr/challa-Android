package com.happyhouse.challa.presentation.gallery

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryFilmSlotUiModel
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.ceil

@HiltViewModel(assistedFactory = GalleryViewModel.Factory::class)
class GalleryViewModel @AssistedInject constructor(
    @Assisted private val roomId: Long,
) : BaseViewModel<GalleryState, GalleryIntent, GallerySideEffect>(
        initialState = GalleryState(roomId = roomId),
    ) {
    private var countdownJob: Job? = null

    // TODO: 실제 API 붙으면 서버가 내려주는 인화 완료 시각으로 교체할 것.
    private var printCompleteAtMillis: Long = 0L

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
        countdownJob?.cancel()

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
                if (photoInfo is PhotoInfo.Waiting) {
                    startCountdown()
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

    /**
     * 인화 완료까지 남은 시간을 1초마다 갱신한다.
     *
     * 남은 초를 1씩 빼지 않고 매번 완료 시각과 현재 시각을 비교한다.
     * 화면이 백그라운드로 내려갔다 돌아와도 값이 어긋나지 않게 하기 위함이다.
     */
    private fun startCountdown() {
        countdownJob =
            viewModelScope.launch {
                while (isActive) {
                    val remainingSeconds = remainingSecondsUntilPrintComplete()
                    updateState {
                        val waiting = photoInfo as? PhotoInfo.Waiting ?: return@updateState this
                        copy(photoInfo = waiting.copy(remainingSeconds = remainingSeconds))
                    }

                    if (remainingSeconds <= 0L) {
                        // 인화가 끝났으니 공개된 사진을 다시 받아온다.
                        handlePhotosLoad()
                        break
                    }
                    delay(COUNTDOWN_TICK_MS)
                }
            }
    }

    private fun remainingSecondsUntilPrintComplete(): Long {
        val remainingMillis = printCompleteAtMillis - System.currentTimeMillis()
        if (remainingMillis <= 0L) return 0L

        // 남은 시간이 잘려서 실제보다 짧게 보이지 않도록 올림한다.
        return ceil(remainingMillis.toDouble() / MILLIS_PER_SECOND).toLong()
    }

    // TODO: 실제 API 연동 전까지 쓰는 mock 데이터. 인화 여부도 서버 응답으로 판단하도록 교체할 것.
    private suspend fun loadMockPhotoInfo(): PhotoInfo {
        delay(MOCK_LOAD_DELAY_MS) // TODO: 로딩 상태 확인용으로 실제 API 붙으면 제거하기
        printCompleteAtMillis = System.currentTimeMillis() + MOCK_REMAINING_SECONDS * MILLIS_PER_SECOND
        return PhotoInfo.Waiting(
            slots = loadMockFilmSlots(),
            remainingSeconds = MOCK_REMAINING_SECONDS,
        )
    }

    // TODO: 실제 API 연동 전까지 쓰는 mock 필름 슬롯
    private fun loadMockFilmSlots(): ImmutableList<GalleryFilmSlotUiModel> =
        (0 until MOCK_PHOTO_COUNT)
            .map { index ->
                GalleryFilmSlotUiModel(
                    order = index + 1,
                    imageUrl = "https://picsum.photos/seed/${roomId}_$index/300/400",
                )
            }.toPersistentList()

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
        private const val MILLIS_PER_SECOND = 1_000L
        private const val COUNTDOWN_TICK_MS = 1_000L

        private const val MOCK_PHOTO_COUNT = 24
        private const val MOCK_MEMBER_COUNT = 6
        private const val MOCK_REMAINING_SECONDS = 10_798L
        private const val MOCK_LOAD_DELAY_MS = 300L
        private const val MOCK_ROOM_NAME = "다낭 4박5일"
    }
}
