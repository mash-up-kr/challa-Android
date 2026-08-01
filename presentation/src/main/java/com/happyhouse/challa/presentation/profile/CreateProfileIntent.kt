package com.happyhouse.challa.presentation.profile

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface CreateProfileIntent : UiIntent {
    data class NicknameChanged(
        val nickname: String,
    ) : CreateProfileIntent

    data class ProfileImageSelected(
        val uri: String,
    ) : CreateProfileIntent

    data object ProfileImageDeleteClick : CreateProfileIntent

    data object DoneClick : CreateProfileIntent
}
