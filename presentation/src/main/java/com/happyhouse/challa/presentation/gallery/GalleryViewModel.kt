package com.happyhouse.challa.presentation.gallery

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.model.RoomDetail
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.domain.model.RoomUser
import com.happyhouse.challa.domain.repository.PhotoRepository
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.causeOrNull
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryIntent
import com.happyhouse.challa.presentation.gallery.contract.GalleryMemberUiModel
import com.happyhouse.challa.presentation.gallery.contract.GallerySideEffect
import com.happyhouse.challa.presentation.gallery.contract.GalleryState
import com.happyhouse.challa.presentation.gallery.contract.GalleryState.PhotoInfo
import com.happyhouse.challa.presentation.gallery.util.toPhotoInfo
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import kotlin.math.ceil

@HiltViewModel(assistedFactory = GalleryViewModel.Factory::class)
class GalleryViewModel @AssistedInject constructor(
    @Assisted private val roomId: Long,
    private val roomRepository: RoomRepository,
    private val photoRepository: PhotoRepository,
) : BaseViewModel<GalleryState, GalleryIntent, GallerySideEffect>(
        initialState = GalleryState(roomId = roomId),
    ) {
    private var loadJob: Job? = null
    private var countdownJob: Job? = null

    /** 서버가 내려준 인화 완료 시각. 아직 정해지지 않았으면 null */
    private var printCompletedAt: Instant? = null

    /** 완료 시각이 지났는데도 상태가 인화 대기일 때 다시 확인한 횟수 */
    private var printStatusRecheckCount = 0

    init {
        onIntent(GalleryIntent.PhotosLoad)
    }

    override fun onIntent(intent: GalleryIntent) {
        when (intent) {
            GalleryIntent.PhotosLoad -> handlePhotosLoad()
            is GalleryIntent.PhotoClick -> handlePhotoClick(intent.photoId)
            GalleryIntent.PrintCountdownClick -> handlePrintCountdownClick()
            GalleryIntent.ShootClick -> handleShootClick()
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

                // 방 정보와 사진 목록이 둘 다 있어야 화면을 그릴 수 있으므로 함께 요청한다.
                val roomDeferred = async { roomRepository.getRoom(roomId) }
                val photosDeferred = async { photoRepository.getPhotos(roomId) }
                val usersDeferred = async { roomRepository.getRoomUsers(roomId) }
                val roomResult = roomDeferred.await()
                val photosResult = photosDeferred.await()

                if (roomResult !is ChallaResult.Success || photosResult !is ChallaResult.Success) {
                    Timber.e(
                        roomResult.causeOrNull() ?: photosResult.causeOrNull(),
                        "갤러리를 불러오지 못했습니다. room=$roomResult, photos=$photosResult",
                    )
                    // 본문이 에러면 프로필 바도 그리지 않으므로 참여자 조회를 끝까지 기다릴 이유가 없다.
                    usersDeferred.cancel()
                    updateState { copy(photoInfo = PhotoInfo.Error) }
                    return@launch
                }

                val room = roomResult.data
                printCompletedAt = room.photoPrintCompletedAt
                val remainingSeconds = remainingSecondsUntilPrintComplete()

                updateState {
                    copy(
                        roomName = room.title,
                        photoInfo = room.toPhotoInfo(photosResult.data, remainingSeconds),
                    )
                }

                updateMembersWhenReady(usersDeferred)
                startCountdownIfNeeded(room, remainingSeconds)
            }
    }

    /**
     * 참여자를 받는 대로 프로필 바에 채운다.
     *
     * 참여자는 사진 위에 얹히는 부가 정보라, 조회가 늦어져도 갤러리 본문 표시를 붙잡지 않도록
     * 본문을 그린 뒤 따로 기다린다. 실패하면 본문은 그대로 두고 프로필 바만 비운다.
     */
    private fun CoroutineScope.updateMembersWhenReady(usersDeferred: Deferred<ChallaResult<List<RoomUser>>>) {
        launch {
            val members =
                when (val usersResult = usersDeferred.await()) {
                    is ChallaResult.Success -> usersResult.data.toGalleryMembers()
                    is ChallaResult.Failure -> {
                        Timber.w(usersResult.causeOrNull(), "방 참여자를 불러오지 못했습니다. users=$usersResult")
                        persistentListOf()
                    }
                }

            updateState { copy(members = members) }
        }
    }

    private fun handlePhotoClick(photoId: Long) {
        viewModelScope.launch {
            sendEffect(GallerySideEffect.NavigateToPhotoDetail(photoId))
        }
    }

    private fun handleShootClick() {
        viewModelScope.launch {
            sendEffect(GallerySideEffect.NavigateToCamera)
        }
    }

    private fun handlePrintCountdownClick() {
        viewModelScope.launch {
            sendEffect(GallerySideEffect.PrintNotCompleted)
        }
    }

    /**
     * 인화 대기 중일 때만 인화 완료를 지켜본다.
     *
     * 남은 시간이 있으면 카운트다운을 돌리고, 완료 시각이 이미 지났는데도 서버 상태가 아직
     * 인화 대기면 잠시 뒤 상태를 다시 확인한다. 이때 카운트다운을 0초에서 시작하면 첫 tick에
     * 곧바로 끝나면서 완료 콜백이 그 자리에서 실행되어 아직 끝나지 않은 조회를 취소한다.
     */
    private fun startCountdownIfNeeded(
        room: RoomDetail,
        remainingSeconds: Long,
    ) {
        if (room.status != RoomStatus.PHOTO_PRINT_PENDING) return

        if (remainingSeconds <= 0L) {
            Timber.w("인화 대기지만 남은 시간이 없어 잠시 뒤 상태를 다시 확인합니다. 완료 시각=${room.photoPrintCompletedAt}")
            schedulePrintStatusRecheck()
            return
        }

        printStatusRecheckCount = 0
        startCountdown()
    }

    /**
     * 인화 완료까지 남은 시간을 1초마다 갱신한다.
     *
     * 남은 초를 1씩 빼지 않고 매번 완료 시각과 현재 시각을 비교한다.
     * 화면이 백그라운드로 내려갔다 돌아와도 값이 어긋나지 않게 하기 위함이다.
     */
    private fun startCountdown() {
        watchPrintCompletion {
            while (true) {
                // 조건과 표시에 같은 값을 쓰도록 한 번만 읽는다.
                val remainingSeconds = remainingSecondsUntilPrintComplete()
                updateRemainingSeconds(remainingSeconds)

                if (remainingSeconds <= 0L) break
                delay(COUNTDOWN_TICK_MS)
            }
        }
    }

    /**
     * 완료 시각은 지났는데 서버 상태가 아직 인화 대기일 때, 잠시 뒤 방 상태를 다시 확인한다.
     *
     * 이 처리가 없으면 화면이 남은 시간 0인 인화 대기로 굳어 재조회할 방법이 없다.
     * 다만 서버가 완료 시각을 과거로 내려주면 상태가 영영 넘어가지 않으므로 확인 횟수를 제한한다.
     */
    private fun schedulePrintStatusRecheck() {
        if (printStatusRecheckCount >= MAX_PRINT_STATUS_RECHECK_COUNT) {
            Timber.w("인화 대기 상태가 풀리지 않아 재조회를 멈춥니다. 완료 시각=$printCompletedAt")
            return
        }

        printStatusRecheckCount++
        watchPrintCompletion {
            delay(PRINT_STATUS_RECHECK_MS)
        }
    }

    /**
     * 인화 완료를 지켜보는 작업을 걸고, 정상적으로 끝나면 공개된 사진을 다시 받아온다.
     *
     * 재조회를 작업 코루틴 안에서 부르면 자기 자신을 취소하게 되므로 완료된 뒤에 잇는다.
     * 취소로 끝난 경우(cause != null)는 재조회하지 않는다.
     */
    private fun watchPrintCompletion(block: suspend () -> Unit) {
        val job = viewModelScope.launch { block() }
        // 콜백에서 countdownJob을 취소하므로 대입을 먼저 끝낸다.
        countdownJob = job

        job.invokeOnCompletion { cause ->
            if (cause == null) handlePhotosLoad(showLoading = false)
        }
    }

    private fun updateRemainingSeconds(remainingSeconds: Long) {
        updateState {
            // 카운트다운 취소와 상태 변경이 엇갈리면 인화 대기가 아닌 상태로 들어올 수 있다.
            val waiting =
                photoInfo as? PhotoInfo.Waiting
                    ?: run {
                        Timber.w("인화 대기 상태가 아니라 남은 시간 갱신을 건너뜁니다: $photoInfo")
                        return@updateState this
                    }
            copy(photoInfo = waiting.copy(remainingSeconds = remainingSeconds))
        }
    }

    private fun remainingSecondsUntilPrintComplete(): Long {
        val completedAt = printCompletedAt ?: return 0L
        val remainingMillis = completedAt.toEpochMilli() - System.currentTimeMillis()
        if (remainingMillis <= 0L) return 0L

        // 남은 시간이 잘려서 실제보다 짧게 보이지 않도록 올림한다.
        return ceil(remainingMillis.toDouble() / MILLIS_PER_SECOND).toLong()
    }

    @AssistedFactory
    interface Factory {
        fun create(roomId: Long): GalleryViewModel
    }

    companion object {
        private const val MILLIS_PER_SECOND = 1_000L
        private const val COUNTDOWN_TICK_MS = 1_000L

        /** 완료 시각이 지났는데도 서버 상태가 인화 대기일 때 다시 확인하기까지 기다리는 시간 */
        private const val PRINT_STATUS_RECHECK_MS = 5_000L

        private const val MAX_PRINT_STATUS_RECHECK_COUNT = 5
    }
}

private fun List<RoomUser>.toGalleryMembers(): ImmutableList<GalleryMemberUiModel> =
    map { user ->
        GalleryMemberUiModel(
            id = user.id,
            profileImageUrl = user.profileImageUrl,
        )
    }.toPersistentList()
