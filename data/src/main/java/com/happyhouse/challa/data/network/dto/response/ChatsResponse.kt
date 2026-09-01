package com.happyhouse.challa.data.network.dto.response

import com.happyhouse.challa.data.network.parseServerInstant
import com.happyhouse.challa.domain.model.chat.Chat
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

internal fun ChatsResponse.ChatItem.toChat(): Chat =
    when (this) {
        is ChatsResponse.ChatItem.Default ->
            Chat.Default(
                id = chatId,
                userId = userId,
                content = content,
                createdAt = createdAt.parseServerInstant(),
                userName = userName,
                userProfileImageUrl = userProfileImageUrl,
            )

        is ChatsResponse.ChatItem.Emoji ->
            Chat.Emoji(
                id = chatId,
                userId = userId,
                content = content,
                photoId = photoId,
                photoImageUrl = photoImageUrl,
                createdAt = createdAt.parseServerInstant(),
                userName = userName,
                userProfileImageUrl = userProfileImageUrl,
            )

        is ChatsResponse.ChatItem.Comment ->
            Chat.Comment(
                id = chatId,
                userId = userId,
                content = content,
                photoId = photoId,
                photoImageUrl = photoImageUrl,
                createdAt = createdAt.parseServerInstant(),
                userName = userName,
                userProfileImageUrl = userProfileImageUrl,
            )
    }
