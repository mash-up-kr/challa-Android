package com.happyhouse.challa.presentation.camera.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface CameraSideEffect : UiSideEffect {
    data object FlashNotAvailable : CameraSideEffect

    data object FlashEnabled : CameraSideEffect

    data object FlashDisabled : CameraSideEffect
}
