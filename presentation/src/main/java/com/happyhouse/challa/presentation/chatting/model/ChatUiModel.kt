package com.happyhouse.challa.presentation.chatting.model

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.domain.model.chat.Chat
import com.happyhouse.challa.domain.model.chat.ChatType

@Immutable
data class ChatUiModel(
    val type: ChatType,
    val content: String,
    val photoImageUrl: String?,
    val userName: String?,
    val userProfileImageUrl: String?,
)

internal fun Chat.toUiModel(): ChatUiModel =
    ChatUiModel(
        type = type,
        content = content,
        photoImageUrl = photoImageUrl,
        userName = userName,
        userProfileImageUrl = userProfileImageUrl,
    )
