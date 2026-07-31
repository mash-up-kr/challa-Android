package com.happyhouse.challa.presentation.setting.theme.contract

import com.happyhouse.challa.domain.model.PrimaryTheme
import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface ThemeSideEffect : UiSideEffect {
    data class SaveFailed(
        val theme: PrimaryTheme,
    ) : ThemeSideEffect
}
