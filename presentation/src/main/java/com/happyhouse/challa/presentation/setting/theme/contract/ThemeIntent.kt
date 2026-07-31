package com.happyhouse.challa.presentation.setting.theme.contract

import com.happyhouse.challa.domain.model.PrimaryTheme
import com.happyhouse.challa.presentation.base.UiIntent

sealed interface ThemeIntent : UiIntent {
    data class ThemeSelect(
        val theme: PrimaryTheme,
    ) : ThemeIntent
}
