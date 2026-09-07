package com.happyhouse.challa.presentation.photodetail

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.event.RoomEvent
import com.happyhouse.challa.domain.model.PhotoPage
import com.happyhouse.challa.domain.model.PhotoReaction
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.domain.model.toStickerReactions
import com.happyhouse.challa.domain.repository.ChatRepository
import com.happyhouse.challa.domain.repository.PhotoRepository
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.repository.UserRepository
import com.happyhouse.challa.domain.result.causeOrNull
import com.happyhouse.challa.domain.result.onFailure
import com.happyhouse.challa.domain.result.onSuccess
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.navigation.PhotoDetailArgs
import com.happyhouse.challa.presentation.navigation.toPhotos
import com.happyhouse.challa.presentation.photodetail.contract.MAX_STICKER_USER_COUNT
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailIntent
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailSideEffect
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState.PhotoInfo
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailUiModel
import com.happyhouse.challa.presentation.photodetail.contract.PhotoReactionUiModel
import com.happyhouse.challa.presentation.photodetail.contract.REACTION_BURST_DURATION_MILLIS
import com.happyhouse.challa.presentation.photodetail.contract.ReactionBurstUiModel
import com.happyhouse.challa.presentation.photodetail.util.toPhotoDetailUiModels
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = PhotoDetailViewModel.Factory::class)
class PhotoDetailViewModel @AssistedInject constructor(
    @Assisted("roomId") private val roomId: Long,
    @Assisted args: PhotoDetailArgs,
    private val photoRepository: PhotoRepository,
    private val roomRepository: RoomRepository,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
) : BaseViewModel<PhotoDetailState, PhotoDetailIntent, PhotoDetailSideEffect>(
        initialState = initialPhotoDetailState(args),
    ) {
    private var appendJob: Job? = null

    /** 사진별 반응 조회. 넘길 때마다 겹쳐 돌지 않게 붙잡아 둔다. */
    private val reactionJobs = mutableMapOf<Long, Job>()

    /** 사진별 반응 조회 회차. 늦게 도착한 이전 응답을 가려내는 데 쓴다. */
    private val reactionRevisions = mutableMapOf<Long, Int>()

    private val loadedPhotos = args.photos.toPhotos().toMutableList()
    private val loadedPhotoIds = loadedPhotos.mapTo(mutableSetOf()) { photo -> photo.id }
    private var nextPhotoPage = args.nextPhotoPage
    private var hasNextPhotoPage = args.hasNextPhotoPage

    /** 내 반응을 가려내는 기준. 서버 응답에 "내 것" 표시가 없어 userId로 비교한다. */
    private var myUserId: Long? = null

    /** 이번 화면에서 내가 남긴 chatId. 프로필 조회가 실패했을 때의 대비책이다. */
    private val myChatIds = mutableSetOf<Long>()

    /** 지우는 중인 스티커. 연타로 같은 chatId에 삭제가 두 번 나가지 않게 막는다. */
    private val removingChatIds = mutableSetOf<Long>()

    /** 같은 이모지를 다시 남겨도 연출이 재생되도록 매번 새 값을 준다. */
    private var nextBurstId = 0L

    private var burstClearJob: Job? = null

    init {
        observeRoomEvents()
    }

    /** 사진 상세가 열려 있는 동안 방 이름이 바뀌면 제목만 갈아끼운다. 사진을 다시 받을 이유는 없다. */
    private fun observeRoomEvents() {
        viewModelScope.launch {
            roomRepository.roomEventFlow
                .filterIsInstance<RoomEvent.TitleUpdate>()
                .filter { it.roomId == roomId }
                .collect { event -> updateState { copy(roomName = event.title) } }
        }
    }

    override fun onIntent(intent: PhotoDetailIntent) {
        when (intent) {
            PhotoDetailIntent.PhotosLoadMore -> handlePhotosLoadMore()
            is PhotoDetailIntent.ReactionsLoad -> handleReactionsLoad(intent.photo)
            is PhotoDetailIntent.PhotoSave -> handlePhotoSave(intent.photo)
            is PhotoDetailIntent.ReactionClick -> handleReactionClick(intent.photo, intent.emoji)
            is PhotoDetailIntent.StickerClick -> handleStickerClick(intent.photo, intent.reaction)
            is PhotoDetailIntent.MessageChange -> handleMessageChange(intent.message)
            is PhotoDetailIntent.MessageSend -> handleMessageSend(intent.photo)
        }
    }

    private fun handleReactionsLoad(photo: PhotoDetailUiModel) {
        reactionJobs[photo.id]?.let { job -> if (job.isActive) return }

        reactionJobs[photo.id] =
            viewModelScope.launch {
                loadReactions(photo.id)
            }.also { job ->
                // 사진을 넘길수록 끝난 Job이 쌓이지 않게 지운다.
                job.invokeOnCompletion { reactionJobs.remove(photo.id) }
            }
    }

    private fun handlePhotosLoadMore() {
        if (!hasNextPhotoPage) return
        if (appendJob?.isActive == true) return

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

    /** 페이지를 받는 사이에 사진이 늘면 같은 사진이 두 페이지에 걸쳐 오고, 페이저의 key가 겹쳐 깨진다. */
    private fun appendPhotoPage(photoPage: PhotoPage) {
        loadedPhotos += photoPage.photos.filter { photo -> loadedPhotoIds.add(photo.id) }
        hasNextPhotoPage = photoPage.hasNext
        nextPhotoPage++
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
     * 인스타 스토리처럼 같은 이모지도 몇 번이든 다시 보낼 수 있다.
     *
     * 사진에 붙는 스티커는 사람마다 첫 반응 하나뿐이라, 바꾸려면 스티커를 눌러 지우고 새로 보낸다.
     */
    private fun handleReactionClick(
        photo: PhotoDetailUiModel,
        emoji: ReactionEmoji,
    ) {
        if (currentState.photoInfo !is PhotoInfo.Loaded) {
            Timber.w("사진이 로드되지 않아 반응을 남기지 못했습니다: photoId=${photo.id}")
            viewModelScope.launch { sendEffect(PhotoDetailSideEffect.ReactionSendFailed) }
            return
        }

        // 연출은 서버 왕복을 기다리지 않고 누르는 즉시 재생한다. 실패하면 토스트로 따로 알린다.
        emitBurst(photoId = photo.id, emoji = emoji)

        viewModelScope.launch { addReaction(photo = photo, emoji = emoji) }
    }

    private fun handleStickerClick(
        photo: PhotoDetailUiModel,
        reaction: PhotoReactionUiModel,
    ) {
        if (!reaction.isMine) {
            Timber.w("내 스티커가 아니라 지우지 않았습니다. photoId=${photo.id}, chatId=${reaction.chatId}")
            return
        }

        if (!removingChatIds.add(reaction.chatId)) return

        viewModelScope.launch {
            try {
                removeReaction(photoId = photo.id, chatId = reaction.chatId)
            } finally {
                removingChatIds -= reaction.chatId
            }
        }
    }

    /**
     * 연출은 이모지를 고른 그 순간에만 재생한다.
     *
     * 다 재생하면 상태에서 지운다. 남겨두면 사진을 넘겼다 돌아왔을 때 다시 그려지면서 또 터진다.
     */
    private fun emitBurst(
        photoId: Long,
        emoji: ReactionEmoji,
    ) {
        val burst = ReactionBurstUiModel(id = nextBurstId++, photoId = photoId, emoji = emoji)
        updateBurst(burst)

        burstClearJob?.cancel()
        burstClearJob =
            viewModelScope.launch {
                delay(REACTION_BURST_DURATION_MILLIS)
                updateBurst(null)
            }
    }

    private fun updateBurst(burst: ReactionBurstUiModel?) {
        updateState {
            val loaded =
                photoInfo as? PhotoInfo.Loaded
                    ?: run {
                        Timber.w("사진 목록이 열려 있지 않아 반응 연출을 반영하지 않습니다: $photoInfo")
                        return@updateState this
                    }
            copy(photoInfo = loaded.copy(burst = burst))
        }
    }

    private suspend fun addReaction(
        photo: PhotoDetailUiModel,
        emoji: ReactionEmoji,
    ) {
        chatRepository
            .addPhotoReaction(roomId = roomId, photoId = photo.id, emoji = emoji)
            .onSuccess { chatId ->
                myChatIds += chatId
                if (canBecomeSticker(photo.id)) loadReactions(photo.id)
            }.onFailure { failure ->
                Timber.e(failure.causeOrNull(), "반응을 남기지 못했습니다. photoId=${photo.id}, emoji=$emoji")
                sendEffect(PhotoDetailSideEffect.ReactionSendFailed)
            }
    }

    /**
     * 방금 남긴 반응이 스티커로 붙을 수 있는지.
     *
     * 스티커는 사람마다 먼저 남긴 하나씩 [MAX_STICKER_USER_COUNT]명까지라, 내 스티커가 이미 있거나
     * 자리가 다 찼으면 몇 번을 더 보내도 스티커 목록이 그대로다. 그때는 재조회하지 않는다.
     */
    private fun canBecomeSticker(photoId: Long): Boolean {
        val stickers = (currentState.photoInfo as? PhotoInfo.Loaded)?.reactionsOf(photoId) ?: return true

        return stickers.none { sticker -> sticker.isMine } && stickers.size < MAX_STICKER_USER_COUNT
    }

    private suspend fun removeReaction(
        photoId: Long,
        chatId: Long,
    ) {
        chatRepository
            .removePhotoReaction(chatId)
            .onSuccess {
                myChatIds -= chatId
                loadReactions(photoId)
            }.onFailure { failure ->
                Timber.e(failure.causeOrNull(), "반응을 지우지 못했습니다. photoId=$photoId, chatId=$chatId")
                sendEffect(PhotoDetailSideEffect.ReactionCancelFailed)
            }
    }

    /**
     * 남기거나 취소한 뒤에도 목록을 다시 받는다. 그 사이 다른 사람이 남긴 것까지 들어와야
     * 스티커 주인 순서가 서버 기준과 어긋나지 않는다.
     *
     * 이모지를 연달아 누르면 한 사진에 조회가 겹쳐 도는데, 늦게 도착한 이전 응답이 최신 목록을
     * 덮어쓰면 방금 남긴 스티커가 사라진다. 그래서 마지막으로 보낸 요청의 응답만 반영한다.
     */
    private suspend fun loadReactions(photoId: Long) {
        ensureMyUserId()

        val revision = (reactionRevisions[photoId] ?: 0) + 1
        reactionRevisions[photoId] = revision

        chatRepository
            .getPhotoReactions(roomId = roomId, photoId = photoId)
            .onSuccess { reactions ->
                if (reactionRevisions[photoId] == revision) applyReactions(photoId, reactions)
            }.onFailure { failure ->
                Timber.e(failure.causeOrNull(), "반응 목록을 불러오지 못했습니다. photoId=$photoId")
                sendEffect(PhotoDetailSideEffect.ReactionsLoadFailed)
            }
    }

    private fun applyReactions(
        photoId: Long,
        reactions: List<PhotoReaction>,
    ) {
        val stickers =
            reactions
                .toStickerReactions(limit = MAX_STICKER_USER_COUNT)
                .map { reaction ->
                    PhotoReactionUiModel(
                        chatId = reaction.chatId,
                        emoji = reaction.emoji,
                        isMine = reaction.isMine(),
                    )
                }.toPersistentList()

        updateState {
            val loaded =
                photoInfo as? PhotoInfo.Loaded
                    ?: run {
                        Timber.w("사진 목록이 열려 있지 않아 반응을 반영하지 않습니다: $photoInfo")
                        return@updateState this
                    }

            copy(photoInfo = loaded.copy(reactions = (loaded.reactions + (photoId to stickers)).toPersistentMap()))
        }
    }

    /**
     * 실패하면 그 사진에서는 이번 화면에서 남긴 것만 내 반응으로 잡혀 예전 스티커를 지울 수 없다.
     * 반응을 다시 받을 때마다 재시도해 회복한다.
     */
    private suspend fun ensureMyUserId() {
        if (myUserId != null) return

        userRepository
            .getMyProfile()
            .onSuccess { profile -> myUserId = profile.id }
            .onFailure { failure ->
                Timber.w(failure.causeOrNull(), "내 프로필을 불러오지 못해 내 반응을 가려내지 못합니다.")
            }
    }

    private fun PhotoReaction.isMine(): Boolean = userId == myUserId || chatId in myChatIds

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
                    .sendPhotoComment(roomId = roomId, photoId = photo.id, message = message)
                    .onSuccess {
                        updateState { copy(messageInput = "") }
                        sendEffect(PhotoDetailSideEffect.MessageSendSucceeded)
                    }
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
            args: PhotoDetailArgs,
        ): PhotoDetailViewModel
    }
}

private fun initialPhotoDetailState(args: PhotoDetailArgs): PhotoDetailState {
    val photos = args.photos.toPhotos().toPhotoDetailUiModels()

    return PhotoDetailState(
        initialPhotoIndex = args.initialPhotoIndex,
        roomName = args.roomName,
        photoInfo = if (photos.isEmpty()) PhotoInfo.Empty else PhotoInfo.Loaded(photos),
    )
}
