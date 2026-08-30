package com.happyhouse.challa.presentation.chatting

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.model.chat.Chat
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
import kotlinx.coroutines.Job
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
    private var loadJob: Job? = null
    private var nextPage = 0
    private val loadedChats = mutableListOf<Chat>()
    private var currentUserId: Long? = null

    init {
        loadChats()
    }

    override fun onIntent(intent: ChatIntent) {
        when (intent) {
            ChatIntent.ChatsLoad -> loadChats()
            ChatIntent.ChatsLoadMore -> loadChats(loadMore = true)
            is ChatIntent.MessageChange -> updateState { copy(message = intent.message) }
        }
    }

    private fun loadChats(loadMore: Boolean = false) {
        if (loadJob?.isActive == true) return

        val loaded = currentState.chatInfo as? ChatInfo.Loaded
        if (loadMore && (loaded == null || !loaded.hasNext)) return

        if (!loadMore) {
            nextPage = 0
            loadedChats.clear()
            updateState { copy(chatInfo = ChatInfo.Loading) }
        } else {
            updateState { copy(chatInfo = checkNotNull(loaded).copy(isLoadingMore = true)) }
        }

        val requestedPage = nextPage
        loadJob =
            viewModelScope.launch {
                val userId =
                    getCurrentUserId() ?: run {
                        updateState { copy(chatInfo = ChatInfo.Error) }
                        return@launch
                    }

                chatRepository
                    .getChats(roomId = roomId, page = requestedPage)
                    .onSuccess { page ->
                        loadedChats += page.chats
                        val chats =
                            loadedChats
                                .sortedBy { chat -> chat.createdAt }
                                .map { chat -> chat.toUiModel(currentUserId = userId) }

                        nextPage = requestedPage + 1
                        updateState {
                            copy(
                                chatInfo =
                                    ChatInfo.Loaded(
                                        chats = chats.toPersistentList(),
                                        hasNext = page.hasNext,
                                        isLoadingMore = false,
                                    ),
                            )
                        }
                    }.onFailure { failure ->
                        Timber.e(
                            failure.causeOrNull(),
                            "채팅 목록을 불러오지 못했습니다. roomId=$roomId, page=$requestedPage",
                        )
                        if (loadMore) {
                            updateState { copy(chatInfo = loaded?.copy(isLoadingMore = false) ?: chatInfo) }
                        } else {
                            updateState { copy(chatInfo = ChatInfo.Error) }
                        }
                    }
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
}
