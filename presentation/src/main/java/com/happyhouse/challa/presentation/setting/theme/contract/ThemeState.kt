package com.happyhouse.challa.presentation.setting.theme.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import com.happyhouse.challa.presentation.setting.theme.model.ThemeUiModel

@Immutable
data class ThemeState(
    val selectedTheme: ThemeUiModel = ThemeUiModel.LEMONADE,
    val isSaving: Boolean = false,
) : UiState
