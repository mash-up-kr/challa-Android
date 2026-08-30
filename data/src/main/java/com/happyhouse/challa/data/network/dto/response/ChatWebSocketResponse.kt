package com.happyhouse.challa.data.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ChatWebSocketResponse(
    val chat: ChatsResponse.ChatItem,
)
