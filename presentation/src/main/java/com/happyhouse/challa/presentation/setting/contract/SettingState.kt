package com.happyhouse.challa.presentation.setting.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import com.happyhouse.challa.presentation.setting.theme.model.ThemeUiModel

@Immutable
data class SettingState(
    val nickname: String,
    val maskedEmail: String,
    val primaryTheme: ThemeUiModel = ThemeUiModel.LEMONADE,
) : UiState
