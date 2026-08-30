package com.happyhouse.challa.domain.model.chat

sealed interface ChatSubscriptionEvent {
    data object Subscribed : ChatSubscriptionEvent

    data class ChatsReceived(
        val chats: List<Chat>,
    ) : ChatSubscriptionEvent
}
