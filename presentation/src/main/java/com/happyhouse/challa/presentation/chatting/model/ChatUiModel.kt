package com.happyhouse.challa.presentation.chatting.model

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.domain.model.chat.Chat
import java.time.ZoneId
import java.time.ZonedDateTime

@Immutable
sealed interface ChatUiModel {
    val chatId: Long
    val userId: Long
    val createdAt: ZonedDateTime
    val isMine: Boolean
    val userName: String?
    val userProfileImageUrl: String?

    @Immutable
    data class Default(
        override val chatId: Long,
        override val userId: Long,
        val content: String,
        override val createdAt: ZonedDateTime,
        override val isMine: Boolean,
        override val userName: String?,
        override val userProfileImageUrl: String?,
    ) : ChatUiModel

    @Immutable
    data class Emoji(
        override val chatId: Long,
        override val userId: Long,
        val reactionEmoji: ReactionEmoji?,
        val photoImageUrl: String,
        override val createdAt: ZonedDateTime,
        override val isMine: Boolean,
        override val userName: String?,
        override val userProfileImageUrl: String?,
    ) : ChatUiModel

    @Immutable
    data class Comment(
        override val chatId: Long,
        override val userId: Long,
        val content: String,
        val photoImageUrl: String,
        override val createdAt: ZonedDateTime,
        override val isMine: Boolean,
        override val userName: String?,
        override val userProfileImageUrl: String?,
    ) : ChatUiModel
}

internal fun Chat.toUiModel(currentUserId: Long): ChatUiModel =
    when (this) {
        is Chat.Default ->
            ChatUiModel.Default(
                chatId = id,
                userId = userId,
                content = content,
                createdAt = createdAt.atZone(ZoneId.systemDefault()),
                isMine = userId == currentUserId,
                userName = userName,
                userProfileImageUrl = userProfileImageUrl,
            )

        is Chat.Emoji ->
            ChatUiModel.Emoji(
                chatId = id,
                userId = userId,
                reactionEmoji = ReactionEmoji.from(content),
                photoImageUrl = photoImageUrl,
                createdAt = createdAt.atZone(ZoneId.systemDefault()),
                isMine = userId == currentUserId,
                userName = userName,
                userProfileImageUrl = userProfileImageUrl,
            )

        is Chat.Comment ->
            ChatUiModel.Comment(
                chatId = id,
                userId = userId,
                content = content,
                photoImageUrl = photoImageUrl,
                createdAt = createdAt.atZone(ZoneId.systemDefault()),
                isMine = userId == currentUserId,
                userName = userName,
                userProfileImageUrl = userProfileImageUrl,
            )
    }
