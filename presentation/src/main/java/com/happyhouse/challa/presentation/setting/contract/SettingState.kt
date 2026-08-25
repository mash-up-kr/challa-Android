package com.happyhouse.challa.presentation.setting.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import com.happyhouse.challa.presentation.setting.theme.model.ThemeUiModel

@Immutable
data class SettingState(
    val profile: ProfileState = ProfileState.Loading,
    val primaryTheme: ThemeUiModel? = null,
) : UiState {
    @Immutable
    sealed interface ProfileState {
        data object Loading : ProfileState

        data object Error : ProfileState

        data class Loaded(
            val nickname: String,
            val profileImageUrl: String?,
        ) : ProfileState
    }
}
