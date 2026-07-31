package com.happyhouse.challa.presentation.setting.theme.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.domain.model.PrimaryTheme
import com.happyhouse.challa.presentation.base.UiState

@Immutable
data class ThemeState(
    val selectedTheme: PrimaryTheme = PrimaryTheme.LEMONADE,
    val isSaving: Boolean = false,
) : UiState
