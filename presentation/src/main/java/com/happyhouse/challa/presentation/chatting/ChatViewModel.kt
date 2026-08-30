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
    private var nextPage = 0
    private var hasNextPage = false
    private var initialChatsLoaded = false
    private val chatsById = linkedMapOf<Long, Chat>()
    private val bufferedChatsById = linkedMapOf<Long, Chat>()
    private var currentUserId: Long? = null

    init {
        startChatSession()
    }

    override fun onIntent(intent: ChatIntent) {
        when (intent) {
            ChatIntent.ChatsLoad -> startChatSession()
            ChatIntent.ChatsLoadMore -> loadMoreChats()
            is ChatIntent.MessageChange -> updateState { copy(message = intent.message) }
        }
    }

    private fun startChatSession() {
        chatSessionJob?.cancel()
        loadMoreJob?.cancel()
        nextPage = 0
        hasNextPage = false
        initialChatsLoaded = false
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
                                        is ChatSubscriptionEvent.ChatsReceived -> handleRealtimeChats(event.chats)
                                    }
                                }
                            }

                        subscriptionReady.await()
                        val userId =
                            getCurrentUserId() ?: run {
                                socketJob.cancel()
                                updateState { copy(chatInfo = ChatInfo.Error) }
                                return@coroutineScope
                            }

                        loadInitialChats(userId)
                        socketJob.join()
                    }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (throwable: Throwable) {
                    Timber.e(throwable, "채팅 WebSocket 구독이 종료되었습니다. roomId=$roomId")
                    if (!initialChatsLoaded) {
                        updateState { copy(chatInfo = ChatInfo.Error) }
                    }
                }
            }
    }

    private suspend fun loadInitialChats(userId: Long) {
        chatRepository
            .getChats(roomId = roomId, page = INITIAL_PAGE)
            .onSuccess { page ->
                page.chats.forEach { chat -> chatsById[chat.id] = chat }
                bufferedChatsById.values.forEach { chat -> chatsById[chat.id] = chat }
                bufferedChatsById.clear()

                initialChatsLoaded = true
                nextPage = INITIAL_PAGE + 1
                hasNextPage = page.hasNext
                publishChats(userId = userId)
            }.onFailure { failure ->
                Timber.e(
                    failure.causeOrNull(),
                    "채팅 목록을 불러오지 못했습니다. roomId=$roomId, page=$INITIAL_PAGE",
                )
                updateState { copy(chatInfo = ChatInfo.Error) }
            }
    }

    private fun handleRealtimeChats(chats: List<Chat>) {
        if (!initialChatsLoaded) {
            chats.forEach { chat -> bufferedChatsById[chat.id] = chat }
            return
        }

        chats.forEach { chat -> chatsById[chat.id] = chat }
        currentUserId?.let { userId ->
            val isLoadingMore = (currentState.chatInfo as? ChatInfo.Loaded)?.isLoadingMore == true
            publishChats(userId = userId, isLoadingMore = isLoadingMore)
        }
    }

    private fun loadMoreChats() {
        if (loadMoreJob?.isActive == true) return

        val loaded = currentState.chatInfo as? ChatInfo.Loaded ?: return
        if (!initialChatsLoaded || !hasNextPage) return
        val userId = currentUserId ?: return
        val requestedPage = nextPage

        updateState { copy(chatInfo = loaded.copy(isLoadingMore = true)) }
        loadMoreJob =
            viewModelScope.launch {
                chatRepository
                    .getChats(roomId = roomId, page = requestedPage)
                    .onSuccess { page ->
                        page.chats.forEach { chat -> chatsById.putIfAbsent(chat.id, chat) }
                        nextPage = requestedPage + 1
                        hasNextPage = page.hasNext
                        publishChats(userId = userId)
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

    private fun publishChats(
        userId: Long,
        isLoadingMore: Boolean = false,
    ) {
        val chats =
            chatsById.values
                .sortedWith(compareBy(Chat::createdAt, Chat::id))
                .map { chat -> chat.toUiModel(currentUserId = userId) }

        updateState {
            copy(
                chatInfo =
                    ChatInfo.Loaded(
                        chats = chats.toPersistentList(),
                        hasNext = hasNextPage,
                        isLoadingMore = isLoadingMore,
                    ),
            )
        }
    }

    private suspend fun getCurrentUserId(): Long? {
        currentUserId?.let { return it }

        return when (val result = userRepository.getMyProfile()) {
            is ChallaResult.Success -> result.data.id.also { currentUserId = it }
            is ChallaResult.Failure -> {
                Timber.e(result.causeOrNull(), "내 프로필을 불러오지 못해 채팅방을 열 수 없습니다. roomId=$roomId")
                null
            }
        }
    }

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
