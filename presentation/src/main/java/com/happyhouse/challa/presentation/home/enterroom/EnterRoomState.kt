package com.happyhouse.challa.presentation.home.enterroom

import com.happyhouse.challa.presentation.base.UiState

data class EnterRoomState(
    val code: String = "",
    val isSubmitting: Boolean = false,
) : UiState {
    val canSubmit: Boolean
        get() = code.length == ENTER_ROOM_CODE_LENGTH
}
