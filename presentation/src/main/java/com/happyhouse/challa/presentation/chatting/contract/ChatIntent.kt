package com.happyhouse.challa.presentation.chatting.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface ChatIntent : UiIntent {
    data object ChatsLoad : ChatIntent

    data object ChatsLoadMore : ChatIntent

    data object MessageSend : ChatIntent

    data class MessageChange(
        val message: String,
    ) : ChatIntent
}
