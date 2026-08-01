package com.happyhouse.challa.presentation.setting.theme.contract

import com.happyhouse.challa.presentation.base.UiSideEffect
import com.happyhouse.challa.presentation.setting.theme.model.ThemeUiModel

sealed interface ThemeSideEffect : UiSideEffect {
    data object ReadFailed : ThemeSideEffect

    data class SaveFailed(
        val theme: ThemeUiModel,
    ) : ThemeSideEffect
}
