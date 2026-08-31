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
 * WebSocket 구독이 확인된 뒤 채팅 목록을 조회하고 두 경로의 채팅을 [Chat.id]로 병합한다.
 * 목록 조회 전에 수신한 실시간 채팅은 임시 보관해 초기 응답에서 누락되지 않도록 한다.
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
                val subscriptionReady = CompletableDeferred<Unit>()

                try {
                    coroutineScope {
                        val socketJob =
                            launch {
                                chatRepository.observeChats(roomId).collect { event ->
                                    when (event) {
                                        ChatSubscriptionEvent.Subscribed -> subscriptionReady.complete(Unit)
                                        is ChatSubscriptionEvent.ChatReceived -> handleRealtimeChat(event.chat)
                                    }
                                }
                            }

                        subscriptionReady.await()
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
                    Timber.e(throwable, "채팅 WebSocket 구독이 종료되었습니다. roomId=$roomId")
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
        val isLoadingMore = (currentState.chatInfo as? ChatInfo.Loaded)?.isLoadingMore == true
        updateLoadedChatState(context = context, isLoadingMore = isLoadingMore)
    }

    private fun loadNextChatPage() {
        if (loadMoreJob?.isActive == true) return

        val loadedChatInfo = currentState.chatInfo as? ChatInfo.Loaded ?: return
        val context =
            checkNotNull(loadedChatContext) {
                "ChatInfo.Loaded 상태에는 loadedChatContext가 존재해야 합니다. roomId=$roomId"
            }
        if (!context.hasNextPage) return
        val requestedPage = context.nextPage

        updateState { copy(chatInfo = loadedChatInfo.copy(isLoadingMore = true)) }
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
                            copy(chatInfo = latest?.copy(isLoadingMore = false) ?: chatInfo)
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

                        refreshLatestChats()
                    }

                    is ChallaResult.Failure ->
                        Timber.e(result.causeOrNull(), "채팅을 전송하지 못했습니다. roomId=$roomId")
                }
            }
    }

    /** WebSocket echo 유실에 대비해 전송 성공 후 첫 페이지를 다시 병합한다. */
    private suspend fun refreshLatestChats() {
        chatRepository
            .getChats(roomId = roomId, page = INITIAL_PAGE)
            .onSuccess { page ->
                page.chats.forEach { chat -> chatsById[chat.id] = chat }
                val loadedChatInfo = currentState.chatInfo as? ChatInfo.Loaded ?: return@onSuccess
                val context =
                    checkNotNull(loadedChatContext) {
                        "ChatInfo.Loaded 상태에는 loadedChatContext가 존재해야 합니다. roomId=$roomId"
                    }
                updateLoadedChatState(
                    context = context,
                    isLoadingMore = loadedChatInfo.isLoadingMore,
                )
            }.onFailure { failure ->
                Timber.e(
                    failure.causeOrNull(),
                    "전송한 채팅을 다시 불러오지 못했습니다. roomId=$roomId",
                )
            }
    }

    private fun updateLoadedChatState(
        context: LoadedChatContext,
        isLoadingMore: Boolean = false,
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
                        isLoadingMore = isLoadingMore,
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
