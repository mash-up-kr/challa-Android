package com.happyhouse.challa.presentation.chatting.model

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.domain.model.chat.Chat
import com.happyhouse.challa.domain.model.chat.ChatType
import java.time.ZoneId
import java.time.ZonedDateTime

@Immutable
data class ChatUiModel(
    val chatId: Long,
    val userId: Long,
    val type: ChatType,
    val content: String,
    val photoImageUrl: String?,
    val createdAt: ZonedDateTime,
    val isMine: Boolean,
    val userName: String?,
    val userProfileImageUrl: String?,
)

internal fun Chat.toUiModel(currentUserId: Long): ChatUiModel =
    ChatUiModel(
        chatId = id,
        userId = userId,
        type = type,
        content = content,
        photoImageUrl = photoImageUrl,
        createdAt = createdAt.atZone(ZoneId.systemDefault()),
        isMine = userId == currentUserId,
        userName = userName,
        userProfileImageUrl = userProfileImageUrl,
    )
