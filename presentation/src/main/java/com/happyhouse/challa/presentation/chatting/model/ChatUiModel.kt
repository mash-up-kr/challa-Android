package com.happyhouse.challa.presentation.chatting.model

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.domain.model.chat.Chat
import com.happyhouse.challa.domain.model.chat.ChatType

@Immutable
data class ChatUiModel(
    val type: ChatType,
    val content: String,
    val photoImageUrl: String?,
    val isMine: Boolean,
    val userName: String?,
    val userProfileImageUrl: String?,
)

internal fun Chat.toUiModel(currentUserId: Long): ChatUiModel =
    ChatUiModel(
        type = type,
        content = content,
        photoImageUrl = photoImageUrl,
        isMine = userId == currentUserId,
        userName = userName,
        userProfileImageUrl = userProfileImageUrl,
    )
