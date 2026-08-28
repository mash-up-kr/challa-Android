package com.happyhouse.challa.presentation.chatting.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface ChatIntent : UiIntent {
    data class MessageChange(
        val message: String,
    ) : ChatIntent
}
