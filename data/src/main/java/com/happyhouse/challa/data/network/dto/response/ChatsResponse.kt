package com.happyhouse.challa.data.network.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatsResponse(
    val chats: List<ChatItem>,
) {
    @Serializable
    sealed class ChatItem {
        abstract val chatId: Long
        abstract val userId: Long
        abstract val content: String
        abstract val createdAt: String
        abstract val userName: String?
        abstract val userProfileImageUrl: String?

        @Serializable
        @SerialName("DEFAULT")
        data class Default(
            override val chatId: Long,
            override val userId: Long,
            override val content: String,
            override val createdAt: String,
            override val userName: String? = null,
            override val userProfileImageUrl: String? = null,
        ) : ChatItem()

        @Serializable
        @SerialName("EMOJI")
        data class Emoji(
            override val chatId: Long,
            override val userId: Long,
            override val content: String,
            val photoId: Long,
            val photoImageUrl: String,
            override val createdAt: String,
            override val userName: String? = null,
            override val userProfileImageUrl: String? = null,
        ) : ChatItem()

        @Serializable
        @SerialName("COMMENT")
        data class Comment(
            override val chatId: Long,
            override val userId: Long,
            override val content: String,
            val photoId: Long,
            val photoImageUrl: String,
            override val createdAt: String,
            override val userName: String? = null,
            override val userProfileImageUrl: String? = null,
        ) : ChatItem()
    }
}
