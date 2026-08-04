package com.happyhouse.challa.presentation.setting.account.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState

@Immutable
data class AccountState(
    val isLoggingOut: Boolean = false,
) : UiState
