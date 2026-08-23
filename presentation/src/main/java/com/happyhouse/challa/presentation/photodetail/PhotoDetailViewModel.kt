package com.happyhouse.challa.presentation.photodetail

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.model.Photo
import com.happyhouse.challa.domain.model.PhotoPage
import com.happyhouse.challa.domain.model.PhotoReaction
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.domain.model.RoomDetail
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.domain.repository.ChatRepository
import com.happyhouse.challa.domain.repository.PhotoRepository
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.repository.UserRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.causeOrNull
import com.happyhouse.challa.domain.result.onFailure
import com.happyhouse.challa.domain.result.onSuccess
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.photodetail.contract.MAX_STICKER_USER_COUNT
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailIntent
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailSideEffect
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState.PhotoInfo
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailUiModel
import com.happyhouse.challa.presentation.photodetail.contract.PhotoReactionUiModel
import com.happyhouse.challa.presentation.photodetail.contract.ReactionBurstUiModel
import com.happyhouse.challa.presentation.photodetail.util.toPhotoDetailUiModels
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.collections.immutable.toPersistentSet
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
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
) : BaseViewModel<PhotoDetailState, PhotoDetailIntent, PhotoDetailSideEffect>(
        initialState = PhotoDetailState(roomId = roomId, initialPhotoId = initialPhotoId),
    ) {
    private var loadJob: Job? = null

    /** 사진별 반응 조회. 넘길 때마다 겹쳐 돌지 않게 붙잡아 둔다. */
    private val reactionJobs = mutableMapOf<Long, Job>()
    private var appendJob: Job? = null

    /** 지금까지 받아둔 사진 */
    private val loadedPhotos = mutableListOf<Photo>()
    private val loadedPhotoIds = mutableSetOf<Long>()
    private var nextPhotoPage = FIRST_PHOTO_PAGE
    private var hasNextPhotoPage = false

    /** 내 반응을 가려내는 기준. 서버 응답에 "내 것" 표시가 없어 userId로 비교한다. */
    private var myUserId: Long? = null

    /** 이번 화면에서 내가 남긴 chatId. 프로필 조회가 실패했을 때의 대비책이다. */
    private val myChatIds = mutableSetOf<Long>()

    /** 처리 중인 (사진, 이모지). 연타로 중복 요청이 나가지 않게 막는다. */
    private val reactingPhotoEmojis = mutableSetOf<Pair<Long, ReactionEmoji>>()

    /** (사진, 이모지) → 내가 남긴 chatId. 취소할 때 쓴다. */
    private val myReactionChatIds = mutableMapOf<Pair<Long, ReactionEmoji>, Long>()

    /** 같은 이모지를 다시 남겨도 연출이 재생되도록 매번 새 값을 준다. */
    private var nextBurstId = 0L

    init {
        onIntent(PhotoDetailIntent.PhotosLoad)
    }

    override fun onIntent(intent: PhotoDetailIntent) {
        when (intent) {
            PhotoDetailIntent.PhotosLoad -> handlePhotosLoad()
            is PhotoDetailIntent.ReactionsLoad -> handleReactionsLoad(intent.photo)
            PhotoDetailIntent.PhotosLoadMore -> handlePhotosLoadMore()
            is PhotoDetailIntent.PhotoSave -> handlePhotoSave(intent.photo)
            is PhotoDetailIntent.ReactionClick -> handleReactionClick(intent.photo, intent.emoji)
            is PhotoDetailIntent.MessageChange -> handleMessageChange(intent.message)
            is PhotoDetailIntent.MessageSend -> handleMessageSend(intent.photo)
        }
    }

    /** 사진을 넘길 때마다 그 사진의 반응을 받아 온다. 다른 사람이 남긴 것도 함께 들어온다. */
    private fun handleReactionsLoad(photo: PhotoDetailUiModel) {
        reactionJobs[photo.id]?.let { job -> if (job.isActive) return }

        reactionJobs[photo.id] =
            viewModelScope.launch {
                if (myUserId == null) {
                    userRepository
                        .getMyProfile()
                        .onSuccess { profile -> myUserId = profile.id }
                        .onFailure { failure ->
                            // 내 반응을 못 가려내면 링만 안 켜지고 목록은 그대로 보여준다.
                            Timber.w(failure.causeOrNull(), "내 프로필을 불러오지 못해 내 반응을 표시하지 못합니다.")
                        }
                }

                chatRepository
                    .getPhotoReactions(photo.id)
                    .onSuccess { reactions -> applyReactions(photo.id, reactions) }
                    .onFailure { failure ->
                        Timber.e(failure.causeOrNull(), "반응 목록을 불러오지 못했습니다. photoId=${photo.id}")
                        sendEffect(PhotoDetailSideEffect.ReactionSendFailed)
                    }
            }.also { job ->
                // 사진을 넘길수록 끝난 Job이 쌓이지 않게 지운다.
                job.invokeOnCompletion { reactionJobs.remove(photo.id) }
            }
    }

    private fun handlePhotosLoad() {
        // 재시도를 연타하면 조회가 겹쳐 나중에 끝난 응답이 이긴다.
        loadJob?.cancel()
        // 이전 목록에 이어 붙이려던 페이지가 뒤늦게 도착해 섞이지 않게 한다.
        appendJob?.cancel()
        resetPhotoPaging()

        loadJob =
            viewModelScope.launch {
                updateState { copy(photoInfo = PhotoInfo.Loading) }

                val roomDeferred = async { roomRepository.getRoom(roomId) }
                val photosResult = loadPagesUntilInitialPhoto()
                val roomResult = roomDeferred.await()

                photosResult
                    .onSuccess {
                        val room = roomResult.roomOrNull()
                        val photos = loadedPhotos.toPhotoDetailUiModels()

                        // 사진과 제목을 함께 반영해, 제목이 사진보다 늦게 나타나지 않게 한다.
                        updateState {
                            copy(
                                roomName = room?.title ?: roomName,
                                photoInfo = if (photos.isEmpty()) PhotoInfo.Empty else PhotoInfo.Loaded(photos),
                            )
                        }
                    }.onFailure { failure ->
                        Timber.e(failure.causeOrNull(), "사진을 불러오지 못했습니다. roomId=$roomId")
                        updateState { copy(photoInfo = PhotoInfo.Error) }
                        sendEffect(PhotoDetailSideEffect.PhotosLoadFailed)
                    }
            }
    }

    /**
     * 진입한 사진이 나올 때까지 첫 페이지부터 이어 받는다.
     *
     * 갤러리에서 뒤쪽 사진을 눌러 들어오면 그 사진이 첫 페이지에 없어 페이저를 그 자리에서 열 수 없다.
     * 서버가 [PhotoPage.hasNext] 를 계속 true로 내려주면 끝나지 않으므로, 상한에 걸리면 받은 만큼이라도 그린다.
     */
    private suspend fun loadPagesUntilInitialPhoto(): ChallaResult<Unit> {
        repeat(MAX_INITIAL_PHOTO_PAGE_COUNT) {
            val pageResult = photoRepository.getPhotos(roomId, nextPhotoPage)

            when (pageResult) {
                is ChallaResult.Success -> appendPhotoPage(pageResult.data)
                is ChallaResult.Failure -> return pageResult
            }

            if (initialPhotoId in loadedPhotoIds || !hasNextPhotoPage) return ChallaResult.Success(Unit)
        }

        Timber.w(
            "진입한 사진을 ${MAX_INITIAL_PHOTO_PAGE_COUNT}페이지까지 찾지 못해 이어 받기를 멈춥니다. " +
                "roomId=$roomId, photoId=$initialPhotoId",
        )
        return ChallaResult.Success(Unit)
    }

    /** 넘기는 중에 올라오는 신호라 화면을 로딩으로 되돌리지 않고, 받아둔 사진 뒤에만 덧붙인다. */
    private fun handlePhotosLoadMore() {
        if (!hasNextPhotoPage) return
        if (appendJob?.isActive == true || loadJob?.isActive == true) return

        val requestedPage = nextPhotoPage
        appendJob =
            viewModelScope.launch {
                photoRepository
                    .getPhotos(roomId, requestedPage)
                    .onSuccess { photoPage ->
                        appendPhotoPage(photoPage)
                        val photos = loadedPhotos.toPhotoDetailUiModels()

                        updateState {
                            // 이어 받는 동안 다시 조회가 돌면 로딩/에러로 바뀌어 있을 수 있다. 그때는 덮어쓰지 않는다.
                            val loaded =
                                photoInfo as? PhotoInfo.Loaded
                                    ?: run {
                                        Timber.w("사진 목록이 열려 있지 않아 이어 받은 페이지를 반영하지 않습니다: $photoInfo")
                                        return@updateState this
                                    }
                            copy(photoInfo = loaded.copy(photos = photos))
                        }
                    }.onFailure { failure ->
                        Timber.e(
                            failure.causeOrNull(),
                            "다음 사진 페이지를 불러오지 못했습니다. roomId=$roomId, page=$requestedPage",
                        )
                        sendEffect(PhotoDetailSideEffect.PhotosLoadMoreFailed)
                    }
            }
    }

    private fun resetPhotoPaging() {
        loadedPhotos.clear()
        loadedPhotoIds.clear()
        nextPhotoPage = FIRST_PHOTO_PAGE
        hasNextPhotoPage = false
    }

    /** 페이지를 받는 사이에 사진이 늘면 같은 사진이 두 페이지에 걸쳐 오고, 페이저의 key가 겹쳐 깨진다. */
    private fun appendPhotoPage(photoPage: PhotoPage) {
        loadedPhotos += photoPage.photos.filter { photo -> loadedPhotoIds.add(photo.id) }
        hasNextPhotoPage = photoPage.hasNext
        nextPhotoPage++
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
                    Timber.w(causeOrNull(), "방 정보를 불러오지 못했습니다. roomId=$roomId")
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
     * 이미 남긴 이모지를 다시 누르면 취소하고, 아니면 새로 남긴다.
     *
     * 인당 개수 제한은 없다. 사람마다 가장 먼저 남긴 반응 하나만 사진에 스티커로 붙고,
     * 나머지는 채팅 기록에만 쌓인다.
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

        // 연타로 같은 이모지가 두 번 올라가지 않게 막는다.
        if (!reactingPhotoEmojis.add(photo.id to emoji)) return

        val myChatId = myReactionChatIds[photo.id to emoji]

        // 연출은 서버 왕복을 기다리지 않고 누르는 즉시 재생한다. 실패하면 토스트로 따로 알린다.
        if (myChatId == null) emitBurst(photoId = photo.id, emoji = emoji)

        viewModelScope.launch {
            try {
                if (myChatId != null) {
                    cancelReaction(photo = photo, emoji = emoji, chatId = myChatId)
                } else {
                    addReaction(photo = photo, emoji = emoji)
                }
            } finally {
                reactingPhotoEmojis -= photo.id to emoji
            }
        }
    }

    private fun emitBurst(
        photoId: Long,
        emoji: ReactionEmoji,
    ) {
        updateState {
            val loaded = photoInfo as? PhotoInfo.Loaded ?: return@updateState this
            copy(
                photoInfo =
                    loaded.copy(burst = ReactionBurstUiModel(id = nextBurstId++, photoId = photoId, emoji = emoji)),
            )
        }
    }

    private suspend fun addReaction(
        photo: PhotoDetailUiModel,
        emoji: ReactionEmoji,
    ) {
        chatRepository
            .sendPhotoReaction(roomId = roomId, photoId = photo.id, emoji = emoji)
            .onSuccess { chatId ->
                myChatIds += chatId
                reloadReactions(photo.id)
            }.onFailure { failure ->
                Timber.e(failure.causeOrNull(), "반응을 남기지 못했습니다. photoId=${photo.id}, emoji=$emoji")
                sendEffect(PhotoDetailSideEffect.ReactionSendFailed)
            }
    }

    private suspend fun cancelReaction(
        photo: PhotoDetailUiModel,
        emoji: ReactionEmoji,
        chatId: Long,
    ) {
        chatRepository
            .deletePhotoReaction(chatId)
            .onSuccess {
                myChatIds -= chatId
                reloadReactions(photo.id)
            }.onFailure { failure ->
                Timber.e(failure.causeOrNull(), "반응을 취소하지 못했습니다. photoId=${photo.id}, chatId=$chatId")
                sendEffect(PhotoDetailSideEffect.ReactionSendFailed)
            }
    }

    /**
     * 반응을 남기거나 취소한 뒤, 그 사진의 반응을 서버에서 다시 받아 반영한다.
     *
     * 내가 누른 사이 다른 사람이 남긴 것도 함께 들어와, 스티커 주인 순서가 서버 기준과 어긋나지 않는다.
     */
    private suspend fun reloadReactions(photoId: Long) {
        chatRepository
            .getPhotoReactions(photoId)
            .onSuccess { reactions -> applyReactions(photoId, reactions) }
            .onFailure { failure ->
                Timber.e(failure.causeOrNull(), "반응 목록을 다시 불러오지 못했습니다. photoId=$photoId")
                sendEffect(PhotoDetailSideEffect.ReactionSendFailed)
            }
    }

    /** 사람마다 첫 반응만 남기고, 먼저 남긴 순으로 [MAX_STICKER_USER_COUNT]명까지 스티커로 그린다. */
    private fun applyReactions(
        photoId: Long,
        reactions: List<PhotoReaction>,
    ) {
        val stickers =
            reactions
                .distinctBy { reaction -> reaction.userId }
                .take(MAX_STICKER_USER_COUNT)
                .map { reaction -> PhotoReactionUiModel(chatId = reaction.chatId, emoji = reaction.emoji) }
                .toPersistentList()

        // 프로필 조회가 실패해도 이번 화면에서 남긴 건 알아볼 수 있어야, 같은 이모지가 중복으로 쌓이지 않는다.
        val myReactions = reactions.filter { it.userId == myUserId || it.chatId in myChatIds }
        val myEmojis = myReactions.mapTo(mutableSetOf()) { reaction -> reaction.emoji }.toPersistentSet()

        // 취소할 때 chatId가 필요하다. 같은 이모지를 여러 번 남겼다면 가장 먼저 남긴 것을 지운다.
        myReactionChatIds.keys.removeAll { key -> key.first == photoId }
        myReactions.reversed().forEach { reaction ->
            myReactionChatIds[photoId to reaction.emoji] = reaction.chatId
        }

        updateState {
            val loaded =
                photoInfo as? PhotoInfo.Loaded
                    ?: run {
                        Timber.w("사진 목록이 열려 있지 않아 반응을 반영하지 않습니다: $photoInfo")
                        return@updateState this
                    }

            copy(
                photoInfo =
                    loaded.copy(
                        reactions = (loaded.reactions + (photoId to stickers)).toPersistentMap(),
                        myEmojis = (loaded.myEmojis + (photoId to myEmojis)).toPersistentMap(),
                    ),
            )
        }
    }

    private fun handleMessageChange(message: String) {
        updateState { copy(messageInput = message) }
    }

    /** 보낸 뒤 입력만 비우고 키보드는 유지한다. 실패하면 다시 보낼 수 있게 입력을 남겨둔다. */
    private fun handleMessageSend(photo: PhotoDetailUiModel) {
        val message = currentState.messageInput.trim()
        if (message.isEmpty() || currentState.isSendingMessage) {
            Timber.w("보낼 수 없는 메시지라 전송하지 않았습니다: photoId=${photo.id}")
            return
        }

        updateState { copy(isSendingMessage = true) }
        viewModelScope.launch {
            try {
                chatRepository
                    .sendPhotoMessage(roomId = roomId, photoId = photo.id, message = message)
                    .onSuccess { updateState { copy(messageInput = "") } }
                    .onFailure { failure ->
                        // 메시지 본문은 개인정보라 로그에 남기지 않는다.
                        Timber.e(
                            failure.causeOrNull(),
                            "사진 메시지를 보내지 못했습니다. photoId=${photo.id}, length=${message.length}",
                        )
                        sendEffect(PhotoDetailSideEffect.MessageSendFailed)
                    }
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

    companion object {
        private const val FIRST_PHOTO_PAGE = 0

        /** 진입한 사진을 찾느라 이어 받을 페이지 상한 */
        private const val MAX_INITIAL_PHOTO_PAGE_COUNT = 10
    }
}
