package com.happyhouse.challa.presentation.login

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState

@Immutable
data class LoginState(
    val isLoading: Boolean = false,
) : UiState
