package com.happyhouse.challa.presentation.gallery

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.model.Photo
import com.happyhouse.challa.domain.model.PhotoPage
import com.happyhouse.challa.domain.model.RoomDetail
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.domain.model.RoomUser
import com.happyhouse.challa.domain.repository.PhotoRepository
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.repository.RoomVisitRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.causeOrNull
import com.happyhouse.challa.domain.result.onFailure
import com.happyhouse.challa.domain.result.onSuccess
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryIntent
import com.happyhouse.challa.presentation.gallery.contract.GalleryMemberUiModel
import com.happyhouse.challa.presentation.gallery.contract.GallerySideEffect
import com.happyhouse.challa.presentation.gallery.contract.GalleryState
import com.happyhouse.challa.presentation.gallery.contract.GalleryState.PhotoInfo
import com.happyhouse.challa.presentation.gallery.util.toPhotoInfo
import com.happyhouse.challa.presentation.navigation.PhotoDetailArgs
import com.happyhouse.challa.presentation.navigation.toPhotoArgs
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
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
    private val roomVisitRepository: RoomVisitRepository,
) : BaseViewModel<GalleryState, GalleryIntent, GallerySideEffect>(
        initialState = GalleryState(roomId = roomId),
    ) {
    private var loadJob: Job? = null
    private var appendJob: Job? = null
    private var membersJob: Job? = null
    private var countdownJob: Job? = null

    /** 지금까지 받아둔 사진 */
    private val loadedPhotos = mutableListOf<Photo>()
    private val loadedPhotoIds = mutableSetOf<Long>()
    private var nextPhotoPage = FIRST_PHOTO_PAGE
    private var hasNextPhotoPage = false

    private var hasPendingLoadMore = false

    /** 이어 받은 사진으로 본문을 다시 만들 때 쓴다. */
    private var loadedRoom: RoomDetail? = null

    /** 서버가 내려준 인화 완료 시각. 아직 정해지지 않았으면 null */
    private var printCompletedAt: Instant? = null

    /** 참여자 조회 실패를 이미 알렸는지. 실패가 이어지는 동안 토스트가 반복되지 않게 한다. */
    private var hasNotifiedMembersFailure = false

    /** 완료 시각이 지났는데도 상태가 인화 대기일 때 다시 확인한 횟수 */
    private var printStatusRecheckCount = 0

    /** 인화 상태 재조회로 방 정보를 다시 받아도 초대 메뉴가 또 열리지 않게 한다. */
    private var hasHandledFirstVisit = false

    init {
        onIntent(GalleryIntent.PhotosLoad)
    }

    override fun onIntent(intent: GalleryIntent) {
        when (intent) {
            GalleryIntent.PhotosLoad -> handlePhotosLoad()
            GalleryIntent.PhotosLoadMore -> handlePhotosLoadMore()
            GalleryIntent.MembersRefresh -> loadMembers()
            is GalleryIntent.PhotoClick -> handlePhotoClick(intent.photoId)
            GalleryIntent.ProfileBarClick -> handleProfileBarClick()
            GalleryIntent.InviteMenuDismiss -> handleInviteMenuDismiss()
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
        // 이전 목록에 이어 붙이려던 페이지가 뒤늦게 도착해 섞이지 않게 한다.
        appendJob?.cancel()

        loadMembers()

        loadJob =
            viewModelScope.launch {
                if (showLoading) {
                    updateState { copy(photoInfo = PhotoInfo.Loading) }
                }

                // 방 정보와 사진 목록이 둘 다 있어야 화면을 그릴 수 있으므로 함께 요청한다.
                val roomDeferred = async { roomRepository.getRoom(roomId) }
                val photosDeferred = async { photoRepository.getPhotos(roomId, FIRST_PHOTO_PAGE) }
                val roomResult = roomDeferred.await()
                val photosResult = photosDeferred.await()

                if (roomResult !is ChallaResult.Success || photosResult !is ChallaResult.Success) {
                    Timber.e(
                        roomResult.causeOrNull() ?: photosResult.causeOrNull(),
                        "갤러리를 불러오지 못했습니다. roomResult=$roomResult, photosResult=$photosResult",
                    )
                    // 본문이 에러면 프로필 바도 그리지 않으므로 참여자 조회를 끝까지 기다릴 이유가 없다.
                    membersJob?.cancel()
                    // 에러 화면을 그린 뒤 보류 요청이 이어지면 낡은 목록이 에러 화면을 덮는다.
                    hasPendingLoadMore = false
                    updateState { copy(photoInfo = PhotoInfo.Error) }
                    return@launch
                }

                val room = roomResult.data
                loadedRoom = room
                printCompletedAt = room.photoPrintCompletedAt
                val remainingSeconds = remainingSecondsUntilPrintComplete()

                resetPhotoPaging()
                appendPhotoPage(photosResult.data)

                updateState {
                    copy(
                        roomName = room.title,
                        invitationCode = room.invitationCode,
                        photoInfo = room.toPhotoInfo(loadedPhotos, remainingSeconds, hasNextPhotoPage),
                    )
                }

                startCountdownIfNeeded(room, remainingSeconds)
                openInviteMenuIfFirstVisit()
            }
        loadJob?.invokeOnCompletion(::loadPendingPageIfNeeded)
    }

    /** 스크롤에서 올라오는 신호라 화면을 로딩으로 되돌리지 않고, 받아둔 사진 뒤에만 덧붙인다. */
    private fun handlePhotosLoadMore() {
        if (!hasNextPhotoPage) {
            hasPendingLoadMore = false
            return
        }

        // 그리드가 한 화면에 다 들어가면 스크롤이 없어, 여기서 버린 요청은 다시 올라오지 않는다.
        if (appendJob?.isActive == true || loadJob?.isActive == true) {
            hasPendingLoadMore = true
            return
        }
        hasPendingLoadMore = false

        val room = loadedRoom
        if (room == null) {
            Timber.w("방 정보 없이 다음 사진 페이지 요청이 들어와 건너뜁니다. roomId=$roomId")
            return
        }

        val requestedPage = nextPhotoPage
        appendJob =
            viewModelScope.launch {
                photoRepository
                    .getPhotos(roomId, requestedPage)
                    .onSuccess { photoPage ->
                        appendPhotoPage(photoPage)

                        updateState {
                            copy(
                                photoInfo =
                                    room.toPhotoInfo(
                                        photos = loadedPhotos,
                                        remainingSeconds = remainingSecondsUntilPrintComplete(),
                                        hasNextPhotoPage = hasNextPhotoPage,
                                    ),
                            )
                        }
                    }.onFailure { failure ->
                        Timber.e(
                            failure.causeOrNull(),
                            "다음 사진 페이지를 불러오지 못했습니다. roomId=$roomId, page=$requestedPage",
                        )
                        // 실패한 페이지를 곧바로 다시 요청하면 실패가 이어지는 동안 요청이 반복된다.
                        hasPendingLoadMore = false
                        sendEffect(GallerySideEffect.PhotosLoadMoreFailed)
                    }
            }
        appendJob?.invokeOnCompletion(::loadPendingPageIfNeeded)
    }

    private fun loadPendingPageIfNeeded(cause: Throwable?) {
        if (cause == null && hasPendingLoadMore) handlePhotosLoadMore()
    }

    /** 보류된 요청은 지우지 않는다. 다시 받은 첫 페이지 뒤를 이어 받아야 해서 완료 콜백까지 살아 있어야 한다. */
    private fun resetPhotoPaging() {
        loadedPhotos.clear()
        loadedPhotoIds.clear()
        nextPhotoPage = FIRST_PHOTO_PAGE
        hasNextPhotoPage = false
    }

    /** 페이지를 받는 사이에 사진이 늘면 같은 사진이 두 페이지에 걸쳐 오고, 그리드의 key가 겹쳐 깨진다. */
    private fun appendPhotoPage(photoPage: PhotoPage) {
        loadedPhotos += photoPage.photos.filter { photo -> loadedPhotoIds.add(photo.id) }
        hasNextPhotoPage = photoPage.hasNext
        nextPhotoPage++
    }

    /**
     * 참여자를 받는 대로 프로필 바에 채운다.
     *
     * 본문 조회(loadJob)의 자식으로 두면 참여자 응답이 늦는 동안 loadJob이 살아 있어,
     * 그 사이 올라온 다음 페이지 요청이 [handlePhotosLoadMore] 가드에 걸려 버려진다.
     */
    private fun loadMembers() {
        membersJob?.cancel()
        membersJob =
            viewModelScope.launch {
                roomRepository
                    .getRoomUsers(roomId)
                    .onSuccess { users ->
                        hasNotifiedMembersFailure = false
                        updateState { copy(members = users.toGalleryMembers()) }
                        // 참여자가 방 정보보다 늦게 도착했으면 여기서 첫 진입 안내를 연다.
                        openInviteMenuIfFirstVisit()
                    }.onFailure { failure ->
                        Timber.w(failure.causeOrNull(), "방 참여자를 불러오지 못했습니다. roomId=$roomId")
                        // 이미 그린 프로필 바는 지우지 않는다. 인화 상태 재확인으로 조회가 반복되므로 알림은 한 번만 띄운다.
                        if (hasNotifiedMembersFailure) return@onFailure

                        hasNotifiedMembersFailure = true
                        sendEffect(GallerySideEffect.MembersLoadFailed)
                    }
            }
    }

    /**
     * 방에 처음 들어온 사람에게 초대 메뉴를 열어 초대 코드를 바로 보여준다.
     *
     * 방 정보와 참여자가 모두 있어야 메뉴를 그리므로, 둘 다 도착한 뒤에 기록한다.
     * 그래야 메뉴를 보여주지 못한 진입이 첫 진입을 소진하지 않는다.
     */
    private suspend fun openInviteMenuIfFirstVisit() {
        if (hasHandledFirstVisit) return
        if (loadedRoom == null || currentState.members.isEmpty()) return

        hasHandledFirstVisit = true

        roomVisitRepository
            .markVisited(roomId)
            .onSuccess { isFirstVisit ->
                if (!isFirstVisit) return@onSuccess

                updateState {
                    copy(inviteMenu = GalleryState.InviteMenu.Opened(showsTooltip = true))
                }
            }.onFailure { failure ->
                Timber.w(failure.causeOrNull(), "방 첫 진입 여부를 확인하지 못했습니다. roomId=$roomId")
            }
    }

    private fun handleProfileBarClick() {
        updateState {
            copy(
                inviteMenu =
                    when (inviteMenu) {
                        is GalleryState.InviteMenu.Opened -> GalleryState.InviteMenu.Closed
                        // 직접 연 메뉴에는 툴팁을 붙이지 않는다.
                        GalleryState.InviteMenu.Closed ->
                            GalleryState.InviteMenu.Opened(showsTooltip = false)
                    },
            )
        }
    }

    private fun handleInviteMenuDismiss() {
        updateState { copy(inviteMenu = GalleryState.InviteMenu.Closed) }
    }

    private fun handlePhotoClick(photoId: Long) {
        // 목록을 다시 받는 사이에 눌리면 없는 사진일 수 있다. 화면은 열어주고 첫 사진부터 그린다.
        val initialPhotoIndex = loadedPhotos.indexOfFirst { photo -> photo.id == photoId }
        if (initialPhotoIndex < 0) {
            Timber.w("누른 사진이 받아둔 목록에 없어 첫 사진부터 그립니다. roomId=$roomId, photoId=$photoId")
        }

        viewModelScope.launch {
            sendEffect(
                GallerySideEffect.NavigateToPhotoDetail(
                    args =
                        PhotoDetailArgs(
                            initialPhotoIndex = initialPhotoIndex.coerceAtLeast(0),
                            roomName = currentState.roomName,
                            photos = loadedPhotos.toPhotoArgs(),
                            nextPhotoPage = nextPhotoPage,
                            hasNextPhotoPage = hasNextPhotoPage,
                        ),
                ),
            )
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
        private const val FIRST_PHOTO_PAGE = 0

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
            nickname = user.nickname,
            profileImageUrl = user.profileImageUrl,
        )
    }.toPersistentList()
