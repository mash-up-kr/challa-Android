package com.happyhouse.challa.presentation.profile

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface SettingProfileSideEffect : UiSideEffect {
    data class ProfileCreated(
        val nickname: String,
    ) : SettingProfileSideEffect

    data object ProfileUpdated : SettingProfileSideEffect

    data object ProfileCreateFailed : SettingProfileSideEffect

    data object ProfileUpdateFailed : SettingProfileSideEffect

    data object NicknameLengthExceeded : SettingProfileSideEffect
}
