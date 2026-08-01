package com.happyhouse.challa.presentation.setting.theme.contract

import com.happyhouse.challa.presentation.base.UiIntent
import com.happyhouse.challa.presentation.setting.theme.model.ThemeUiModel

sealed interface ThemeIntent : UiIntent {
    data class ThemeSelect(
        val theme: ThemeUiModel,
    ) : ThemeIntent
}
