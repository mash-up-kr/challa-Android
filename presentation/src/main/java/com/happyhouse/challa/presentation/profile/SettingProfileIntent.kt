package com.happyhouse.challa.presentation.profile

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface SettingProfileIntent : UiIntent {
    data class NicknameChanged(
        val nickname: String,
    ) : SettingProfileIntent

    data class ProfileImageSelected(
        val uri: String,
    ) : SettingProfileIntent

    data object ProfileImageDeleteClick : SettingProfileIntent

    data object DoneClick : SettingProfileIntent
}
