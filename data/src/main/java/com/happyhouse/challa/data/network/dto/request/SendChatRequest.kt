package com.happyhouse.challa.data.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class SendChatRequest(
    val chat: Chat,
) {
    @Serializable
    data class Chat(
        val roomId: Long,
        val type: Type,
        val content: String,
    ) {
        @Serializable
        enum class Type {
            DEFAULT,
        }
    }
}
