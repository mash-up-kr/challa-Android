package com.happyhouse.challa.presentation.setting.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import com.happyhouse.challa.presentation.setting.theme.model.ThemeUiModel

@Immutable
data class SettingState(
    val nickname: String = "",
    val profileImageUrl: String? = null,
    val isProfileLoading: Boolean = false,
    val primaryTheme: ThemeUiModel? = null,
) : UiState
