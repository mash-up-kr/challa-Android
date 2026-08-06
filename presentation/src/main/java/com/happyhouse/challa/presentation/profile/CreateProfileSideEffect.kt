package com.happyhouse.challa.presentation.profile

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface CreateProfileSideEffect : UiSideEffect {
    data class ProfileCreated(
        val nickname: String,
    ) : CreateProfileSideEffect

    data object ProfileUpdated : CreateProfileSideEffect

    data object ProfileCreateFailed : CreateProfileSideEffect

    data object ProfileUpdateFailed : CreateProfileSideEffect

    data object NicknameLengthExceeded : CreateProfileSideEffect
}
