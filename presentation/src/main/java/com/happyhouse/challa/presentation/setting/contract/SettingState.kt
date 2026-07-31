package com.happyhouse.challa.presentation.setting.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState

@Immutable
data class SettingState(
    val nickname: String = "나는야멋쟁이토마토",
    val maskedEmail: String = "juy***@naver.com",
    val themeName: String = "레몬에이드",
) : UiState
