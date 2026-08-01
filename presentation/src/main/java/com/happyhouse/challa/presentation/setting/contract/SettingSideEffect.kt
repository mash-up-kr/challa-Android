package com.happyhouse.challa.presentation.setting.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface SettingSideEffect : UiSideEffect {
    data object ThemeReadFailed : SettingSideEffect
}
