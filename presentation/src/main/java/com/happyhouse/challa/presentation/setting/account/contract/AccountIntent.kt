package com.happyhouse.challa.presentation.setting.account.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface AccountIntent : UiIntent {
    data object ProfileReadRetry : AccountIntent

    data object LogoutClick : AccountIntent

    data object WithdrawalConfirmClick : AccountIntent
}
