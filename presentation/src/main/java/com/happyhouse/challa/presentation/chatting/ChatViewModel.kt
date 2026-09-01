package com.happyhouse.challa.presentation.chatting

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.model.chat.Chat
import com.happyhouse.challa.domain.model.chat.ChatSubscriptionEvent
import com.happyhouse.challa.domain.repository.ChatRepository
import com.happyhouse.challa.domain.repository.UserRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.causeOrNull
import com.happyhouse.challa.domain.result.onFailure
import com.happyhouse.challa.domain.result.onSuccess
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.chatting.contract.ChatIntent
import com.happyhouse.challa.presentation.chatting.contract.ChatSideEffect
import com.happyhouse.challa.presentation.chatting.contract.ChatState
import com.happyhouse.challa.presentation.chatting.contract.ChatState.ChatInfo
import com.happyhouse.challa.presentation.chatting.contract.ChatState.ChatInfo.LoadMoreState
import com.happyhouse.challa.presentation.chatting.model.toUiModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * WebSocket 구독 확인 후 REST로 초기 채팅 목록을 조회하고 [Chat.id] 기준으로 병합한다.
 * 초기 조회 전에 수신한 실시간 채팅은 임시 보관해 초기 응답에서 누락되지 않도록 한다.
 * 초기 조회가 끝난 뒤 재구독되면 최신 페이지를 다시 조회해 연결 공백을 보정한다.
 */
@HiltViewModel(assistedFactory = ChatViewModel.Factory::class)
class ChatViewModel @AssistedInject constructor(
    @Assisted private val roomId: Long,
    @Assisted roomName: String,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
) : BaseViewModel<ChatState, ChatIntent, ChatSideEffect>(
        initialState = ChatState(roomName = roomName),
    ) {
    private var chatSessionJob: Job? = null
    private var loadMoreJob: Job? = null
    private var sendMessageJob: Job? = null
    private var loadedChatContext: LoadedChatContext? = null
    private val chatsById = linkedMapOf<Long, Chat>()
    private val bufferedChatsById = linkedMapOf<Long, Chat>()
    private var isChatSessionStarted = false

    override fun onIntent(intent: ChatIntent) {
        when (intent) {
            ChatIntent.ChatsLoad -> restartChatSession()
            ChatIntent.ChatsLoadMore -> loadNextChatPage()
            ChatIntent.MessageSend -> sendMessage()
            is ChatIntent.MessageChange -> updateState { copy(message = intent.message) }
        }
    }

    /** 화면이 foreground에 진입하면 채팅 구독과 초기 조회를 시작한다. */
    fun startChatSession() {
        if (isChatSessionStarted) return

        isChatSessionStarted = true
        restartChatSession()
    }

    /** 화면이 background로 이동하거나 제거되면 현재 구독을 중단한다. */
    fun pauseChatSession() {
        if (!isChatSessionStarted) return

        isChatSessionStarted = false
        chatSessionJob?.cancel()
        chatSessionJob = null
        loadMoreJob?.cancel()
        loadMoreJob = null
    }

    private fun restartChatSession() {
        if (!isChatSessionStarted) return

        chatSessionJob?.cancel()
        loadMoreJob?.cancel()
        loadedChatContext = null
        chatsById.clear()
        bufferedChatsById.clear()
        updateState { copy(chatInfo = ChatInfo.Loading) }

        chatSessionJob =
            viewModelScope.launch {
                val initialSubscriptionReady = CompletableDeferred<Unit>()

                try {
                    coroutineScope {
                        val socketJob =
                            launch {
                                chatRepository.observeChats(roomId).collect { event ->
                                    when (event) {
                                        ChatSubscriptionEvent.Subscribed -> {
                                            // complete는 최초 구독에서만 true이므로 false이면 재구독이다.
                                            if (!initialSubscriptionReady.complete(Unit) && loadedChatContext != null) {
                                                launch {
                                                    fetchAndMergeLatestChats(
                                                        failureLogMessage =
                                                            "WebSocket 재연결 후 최신 채팅 목록을 동기화하지 " +
                                                                "못했습니다. roomId=$roomId, page=$INITIAL_PAGE",
                                                    )
                                                }
                                            }
                                        }

                                        is ChatSubscriptionEvent.ChatReceived ->
                                            handleRealtimeChat(
                                                event.chat,
                                            )
                                    }
                                }
                            }

                        initialSubscriptionReady.await()
                        val myUserId =
                            resolveCurrentUserId() ?: run {
                                socketJob.cancel()
                                updateState { copy(chatInfo = ChatInfo.Error) }
                                return@coroutineScope
                            }

                        if (!loadInitialChats(myUserId)) {
                            socketJob.cancel()
                            bufferedChatsById.clear()
                            return@coroutineScope
                        }
                        socketJob.join()
                    }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (throwable: Throwable) {
                    Timber.e(throwable, "채팅 WebSocket 구독이 예기치 않게 종료되었습니다. roomId=$roomId")
                    if (loadedChatContext == null) {
                        updateState { copy(chatInfo = ChatInfo.Error) }
                    }
                }
            }
    }

    private suspend fun loadInitialChats(myUserId: Long): Boolean =
        when (val result = chatRepository.getChats(roomId = roomId, page = INITIAL_PAGE)) {
            is ChallaResult.Success -> {
                val page = result.data
                page.chats.forEach { chat -> chatsById[chat.id] = chat }
                bufferedChatsById.values.forEach { chat -> chatsById[chat.id] = chat }
                bufferedChatsById.clear()

                val context =
                    LoadedChatContext(
                        myUserId = myUserId,
                        nextPage = INITIAL_PAGE + 1,
                        hasNextPage = page.hasNext,
                    )
                loadedChatContext = context
                updateLoadedChatState(context)

                true
            }

            is ChallaResult.Failure -> {
                Timber.e(
                    result.causeOrNull(),
                    "채팅 목록을 불러오지 못했습니다. roomId=$roomId, page=$INITIAL_PAGE",
                )
                updateState { copy(chatInfo = ChatInfo.Error) }

                false
            }
        }

    private fun handleRealtimeChat(chat: Chat) {
        val context = loadedChatContext
        if (context == null) {
            bufferedChatsById[chat.id] = chat
            return
        }

        chatsById[chat.id] = chat
        val loadMoreState =
            (currentState.chatInfo as? ChatInfo.Loaded)?.loadMoreState ?: LoadMoreState.IDLE
        updateLoadedChatState(context = context, loadMoreState = loadMoreState)
    }

    private fun loadNextChatPage() {
        if (loadMoreJob?.isActive == true) return

        val loadedChatInfo = currentState.chatInfo as? ChatInfo.Loaded ?: return
        val context = resolveLoadedChatContext() ?: return
        if (!context.hasNextPage) return
        val requestedPage = context.nextPage

        updateState {
            copy(chatInfo = loadedChatInfo.copy(loadMoreState = LoadMoreState.LOADING))
        }
        loadMoreJob =
            viewModelScope.launch {
                chatRepository
                    .getChats(roomId = roomId, page = requestedPage)
                    .onSuccess { page ->
                        page.chats.forEach { chat -> chatsById.putIfAbsent(chat.id, chat) }
                        val updatedContext =
                            context.copy(
                                nextPage = requestedPage + 1,
                                hasNextPage = page.hasNext,
                            )
                        loadedChatContext = updatedContext
                        updateLoadedChatState(updatedContext)
                    }.onFailure { failure ->
                        Timber.e(
                            failure.causeOrNull(),
                            "채팅 목록을 불러오지 못했습니다. roomId=$roomId, page=$requestedPage",
                        )
                        updateState {
                            val latest = chatInfo as? ChatInfo.Loaded
                            copy(
                                chatInfo =
                                    latest?.copy(loadMoreState = LoadMoreState.ERROR)
                                        ?: chatInfo,
                            )
                        }
                    }
            }
    }

    private fun sendMessage() {
        if (loadedChatContext == null || sendMessageJob?.isActive == true) return

        val originalMessage = currentState.message
        val content = originalMessage.trim()
        if (content.isEmpty()) return

        sendMessageJob =
            viewModelScope.launch {
                when (val result = chatRepository.sendChat(roomId = roomId, content = content)) {
                    is ChallaResult.Success -> {
                        if (currentState.message == originalMessage) {
                            updateState { copy(message = "") }
                        }

                        fetchAndMergeLatestChats(
                            failureLogMessage =
                                "채팅 전송 후 최신 채팅 목록을 동기화하지 못했습니다. " +
                                    "roomId=$roomId, page=$INITIAL_PAGE",
                        )
                    }

                    is ChallaResult.Failure -> {
                        Timber.e(result.causeOrNull(), "채팅을 전송하지 못했습니다. roomId=$roomId")
                        sendEffect(ChatSideEffect.MessageSendFailed)
                    }
                }
            }
    }

    /** 최신 채팅 첫 페이지를 조회해 현재 목록에 [Chat.id] 기준으로 병합한다. */
    private suspend fun fetchAndMergeLatestChats(failureLogMessage: String) {
        chatRepository
            .getChats(roomId = roomId, page = INITIAL_PAGE)
            .onSuccess { page ->
                val chatInfo = currentState.chatInfo
                val loadedChatInfo =
                    chatInfo as? ChatInfo.Loaded ?: run {
                        Timber.w(
                            "채팅 목록이 로드된 상태가 아니어서 최신 채팅 조회 결과를 반영하지 않습니다. " +
                                "roomId=$roomId, chatInfo=${chatInfo::class.simpleName}",
                        )
                        return@onSuccess
                    }
                val context = resolveLoadedChatContext() ?: return@onSuccess
                val updatedContext = context.copy(hasNextPage = page.hasNext)
                loadedChatContext = updatedContext
                page.chats.forEach { chat -> chatsById[chat.id] = chat }
                updateLoadedChatState(
                    context = updatedContext,
                    loadMoreState = loadedChatInfo.loadMoreState,
                )
            }.onFailure { failure ->
                Timber.e(
                    failure.causeOrNull(),
                    failureLogMessage,
                )
            }
    }

    private fun resolveLoadedChatContext(): LoadedChatContext? =
        loadedChatContext
            ?: run {
                Timber.e(
                    "ChatInfo.Loaded 상태에 loadedChatContext가 없습니다. roomId=$roomId",
                )
                updateState { copy(chatInfo = ChatInfo.Error) }
                null
            }

    private fun updateLoadedChatState(
        context: LoadedChatContext,
        loadMoreState: LoadMoreState = LoadMoreState.IDLE,
    ) {
        val chats =
            chatsById.values
                .sortedWith(compareBy(Chat::createdAt, Chat::id))
                .map { chat -> chat.toUiModel(currentUserId = context.myUserId) }

        updateState {
            copy(
                chatInfo =
                    ChatInfo.Loaded(
                        chats = chats.toPersistentList(),
                        hasNext = context.hasNextPage,
                        loadMoreState = loadMoreState,
                    ),
            )
        }
    }

    private suspend fun resolveCurrentUserId(): Long? {
        userRepository.profile.value?.id?.let { return it }

        return when (val result = userRepository.getMyProfile()) {
            is ChallaResult.Success -> result.data.id
            is ChallaResult.Failure -> {
                Timber.e(result.causeOrNull(), "내 프로필을 불러오지 못해 채팅방을 열 수 없습니다. roomId=$roomId")
                null
            }
        }
    }

    /**
     * 초기 채팅 로드가 성공한 이후에만 유효한 세션 정보.
     *
     * 이 객체가 존재하면 [myUserId], [nextPage], [hasNextPage]를 함께 사용할 수 있다.
     * `null`이면 초기 로드 전이거나 채팅 세션이 초기화된 상태를 의미한다.
     */
    private data class LoadedChatContext(
        val myUserId: Long,
        val nextPage: Int,
        val hasNextPage: Boolean,
    )

    @AssistedFactory
    interface Factory {
        fun create(
            roomId: Long,
            roomName: String,
        ): ChatViewModel
    }

    private companion object {
        const val INITIAL_PAGE = 0
    }
}
