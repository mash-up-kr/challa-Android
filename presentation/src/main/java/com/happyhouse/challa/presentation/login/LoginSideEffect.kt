package com.happyhouse.challa.presentation.login

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface LoginSideEffect : UiSideEffect {
    data object LoginSuccess : LoginSideEffect

    data object LoginFailed : LoginSideEffect
}
