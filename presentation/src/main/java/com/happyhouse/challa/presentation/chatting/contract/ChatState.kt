package com.happyhouse.challa.presentation.chatting.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState

@Immutable
data class ChatState(
    val roomName: String = "",
    val message: String = "",
    val showsFirstMessageTooltip: Boolean = true,
) : UiState
