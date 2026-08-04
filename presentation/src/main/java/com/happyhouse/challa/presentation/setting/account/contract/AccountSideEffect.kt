package com.happyhouse.challa.presentation.setting.account.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface AccountSideEffect : UiSideEffect {
    data object LogoutSuccess : AccountSideEffect

    data object LogoutFailed : AccountSideEffect
}
