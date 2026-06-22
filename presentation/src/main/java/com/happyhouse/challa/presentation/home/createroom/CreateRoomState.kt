package com.happyhouse.challa.presentation.home.createroom

import com.happyhouse.challa.presentation.base.UiState

data class CreateRoomState(
    val name: String = "",
    val isSubmitting: Boolean = false,
) : UiState {
    val canSubmit: Boolean
        get() = name.trim().isNotEmpty() && !isSubmitting
}
