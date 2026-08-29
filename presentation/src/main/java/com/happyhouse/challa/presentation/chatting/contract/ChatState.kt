package com.happyhouse.challa.presentation.chatting.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import com.happyhouse.challa.presentation.chatting.model.ChatUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class ChatState(
    val roomName: String = "",
    val message: String = "",
    val chatInfo: ChatInfo = ChatInfo.Loading,
) : UiState {
    val showsFirstMessageTooltip: Boolean
        get() = (chatInfo as? ChatInfo.Loaded)?.chats?.isEmpty() == true

    @Immutable
    sealed interface ChatInfo {
        data object Loading : ChatInfo

        data object Error : ChatInfo

        data class Loaded(
            val chats: ImmutableList<ChatUiModel> = persistentListOf(),
            val hasNext: Boolean = false,
            val isLoadingMore: Boolean = false,
        ) : ChatInfo
    }
}
