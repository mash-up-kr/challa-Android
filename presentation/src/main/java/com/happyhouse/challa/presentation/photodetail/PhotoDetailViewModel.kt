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
import kotlinx.collections.immutable.toPersistentSet
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

    /** 처리 중인 (사진, 이모지). 연타로 중복 요청이 나가지 않게 막는다. */
    private val reactingPhotoEmojis = mutableSetOf<Pair<Long, ReactionEmoji>>()

    /** (사진, 이모지) → 내가 남긴 chatId. 취소할 때 쓴다. */
    private val myReactionChatIds = mutableMapOf<Pair<Long, ReactionEmoji>, Long>()

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
            is PhotoDetailIntent.MessageChange -> handleMessageChange(intent.message)
            is PhotoDetailIntent.MessageSend -> handleMessageSend(intent.photo)
        }
    }

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
     * 이미 남긴 이모지를 다시 누르면 취소하고, 아니면 새로 남긴다.
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
            .sendPhotoReaction(roomId = roomId, photoId = photo.id, emoji = emoji)
            .onSuccess { chatId ->
                myChatIds += chatId
                // 재조회가 실패해도 같은 이모지를 다시 누르면 취소로 이어지도록 먼저 잡아둔다.
                myReactionChatIds.putIfAbsent(photo.id to emoji, chatId)
                loadReactions(photo.id)
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
                // 지운 반응이 남아 있으면 다시 눌렀을 때 없는 chatId로 취소를 시도한다.
                myReactionChatIds.remove(photo.id to emoji, chatId)
                loadReactions(photo.id)
            }.onFailure { failure ->
                Timber.e(failure.causeOrNull(), "반응을 취소하지 못했습니다. photoId=${photo.id}, chatId=$chatId")
                sendEffect(PhotoDetailSideEffect.ReactionCancelFailed)
            }
    }

    /**
     * 남기거나 취소한 뒤에도 목록을 다시 받는다. 그 사이 다른 사람이 남긴 것까지 들어와야
     * 스티커 주인 순서가 서버 기준과 어긋나지 않는다.
     *
     * 다른 이모지를 연달아 누르면 한 사진에 조회가 겹쳐 도는데, 늦게 도착한 이전 응답이 최신 목록을
     * 덮어쓰면 방금 남긴 반응의 링이 꺼지고 다시 눌렀을 때 취소 대신 중복 등록이 나간다.
     * 그래서 마지막으로 보낸 요청의 응답만 반영한다.
     */
    private suspend fun loadReactions(photoId: Long) {
        val revision = (reactionRevisions[photoId] ?: 0) + 1
        reactionRevisions[photoId] = revision

        chatRepository
            .getPhotoReactions(photoId)
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
                .map { reaction -> PhotoReactionUiModel(chatId = reaction.chatId, emoji = reaction.emoji) }
                .toPersistentList()

        val myReactions = reactions.filter { it.userId == myUserId || it.chatId in myChatIds }
        val myEmojis = myReactions.mapTo(mutableSetOf()) { reaction -> reaction.emoji }.toPersistentSet()

        // 같은 이모지를 여러 번 남겼다면 가장 먼저 남긴 것이 남도록 뒤에서부터 덮어쓴다.
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
