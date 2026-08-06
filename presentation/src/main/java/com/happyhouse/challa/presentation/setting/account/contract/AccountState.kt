package com.happyhouse.challa.presentation.setting.account.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState

@Immutable
data class AccountState(
    val isLoggingOut: Boolean = false,
    val isWithdrawing: Boolean = false,
) : UiState {
    val isProcessing: Boolean
        get() = isLoggingOut || isWithdrawing
}
