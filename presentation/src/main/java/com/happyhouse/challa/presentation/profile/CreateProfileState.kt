package com.happyhouse.challa.presentation.profile

import com.happyhouse.challa.presentation.base.UiState

data class CreateProfileState(
    val nickname: String = "",
    val profileImageUri: String? = null,
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false,
    val isNicknameLengthExceeded: Boolean = false,
) : UiState {
    val canSubmit: Boolean
        get() = nickname.trim().isNotEmpty() && !isSubmitting && !isCompleted
}
