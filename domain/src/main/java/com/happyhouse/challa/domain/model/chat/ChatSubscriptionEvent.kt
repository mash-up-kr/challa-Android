package com.happyhouse.challa.domain.model.chat

sealed interface ChatSubscriptionEvent {
    data object Subscribed : ChatSubscriptionEvent

    data class ChatReceived(
        val chat: Chat,
    ) : ChatSubscriptionEvent
}
