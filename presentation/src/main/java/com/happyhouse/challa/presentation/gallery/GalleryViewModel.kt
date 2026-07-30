package com.happyhouse.challa.presentation.gallery

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryFilmSlotUiModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryIntent
import com.happyhouse.challa.presentation.gallery.contract.GalleryMemberUiModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryPhotoUiModel
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
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.ceil

@HiltViewModel(assistedFactory = GalleryViewModel.Factory::class)
class GalleryViewModel @AssistedInject constructor(
    @Assisted private val roomId: Long,
) : BaseViewModel<GalleryState, GalleryIntent, GallerySideEffect>(
        initialState = GalleryState(roomId = roomId),
    ) {
    private var loadJob: Job? = null
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

    /**
     * @param showLoading 이미 그리드가 떠 있는 재조회에서는 false. 로딩 화면을 거치면 화면이 깜빡인다.
     */
    private fun handlePhotosLoad(showLoading: Boolean = true) {
        countdownJob?.cancel()
        // 재시도를 연타하면 조회가 겹쳐 나중에 끝난 응답이 이긴다.
        loadJob?.cancel()

        loadJob =
            viewModelScope.launch {
                if (showLoading) {
                    updateState { copy(photoInfo = PhotoInfo.Loading) }
                }
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
        val job =
            viewModelScope.launch {
                while (true) {
                    // 조건과 표시에 같은 값을 쓰도록 한 번만 읽는다.
                    val remainingSeconds = remainingSecondsUntilPrintComplete()
                    updateRemainingSeconds(remainingSeconds)

                    if (remainingSeconds <= 0L) break
                    delay(COUNTDOWN_TICK_MS)
                }
            }
        // 콜백에서 countdownJob을 취소하므로 대입을 먼저 끝낸다.
        countdownJob = job

        // 인화가 끝났으니 공개된 사진을 다시 받아온다.
        // 카운트다운 코루틴 안에서 부르면 자기 자신을 취소하게 되므로 완료된 뒤에 잇는다.
        // 취소로 끝난 경우(cause != null)는 재조회하지 않는다.
        //
        // TODO: 카운트다운이 중단 없이 끝나면 이 콜백이 그 자리에서 동기 실행되고,
        //  handlePhotosLoad가 loadJob(= startCountdown을 호출한 코루틴)을 취소한다.
        //  남은 시간이 0 이하면 Waiting을 만들지 않으므로 지금은 도달하지 않지만,
        //  서버가 '거의 지금'인 완료 시각을 내려주면 드러난다.
        job.invokeOnCompletion { cause ->
            if (cause == null) handlePhotosLoad(showLoading = false)
        }
    }

    private fun updateRemainingSeconds(remainingSeconds: Long) {
        updateState {
            val waiting = photoInfo as? PhotoInfo.Waiting ?: return@updateState this
            copy(photoInfo = waiting.copy(remainingSeconds = remainingSeconds))
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

        // 재조회 때마다 새로 잡으면 카운트다운이 끝나도 인화 대기로 남는다.
        if (printCompleteAtMillis == 0L) {
            printCompleteAtMillis = System.currentTimeMillis() + MOCK_REMAINING_SECONDS * MILLIS_PER_SECOND
        }

        val remainingSeconds = remainingSecondsUntilPrintComplete()
        return if (remainingSeconds <= 0L) {
            PhotoInfo.Printed(photos = loadMockPhotos())
        } else {
            PhotoInfo.Waiting(
                slots = loadMockFilmSlots(),
                remainingSeconds = remainingSeconds,
            )
        }
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

    // TODO: 실제 API 연동 전까지 쓰는 mock 공개 사진
    private fun loadMockPhotos(): ImmutableList<GalleryPhotoUiModel> =
        (0 until MOCK_PHOTO_COUNT)
            .map { index ->
                GalleryPhotoUiModel(
                    id = index.toLong(),
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
