package com.happyhouse.challa.data.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ChatsResponse(
    val chats: List<ChatItem>,
) {
    @Serializable
    data class ChatItem(
        val type: ChatType,
        val content: String,
        val photoId: Long? = null,
        val photoImageUrl: String? = null,
        val createdAt: String,
        val userName: String? = null,
        val userProfileImageUrl: String? = null,
    )

    @Serializable
    enum class ChatType {
        DEFAULT,
        EMOJI,
        COMMENT,
    }
}
