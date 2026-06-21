package com.happyhouse.challa.presentation.login

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface LoginIntent : UiIntent {
    data object LoginClick : LoginIntent
}