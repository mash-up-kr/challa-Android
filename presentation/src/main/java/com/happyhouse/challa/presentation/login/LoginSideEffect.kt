package com.happyhouse.challa.presentation.login

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface LoginSideEffect : UiSideEffect {
    data class LoginSuccess(
        val isNewUser: Boolean,
    ) : LoginSideEffect

    data object LoginFailed : LoginSideEffect
}
