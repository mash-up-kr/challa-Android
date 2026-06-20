package com.happyhouse.challa.presentation.camera.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface CameraUiSideEffect : UiSideEffect {
    data object FlashNotAvailable : CameraUiSideEffect

    data object FlashEnabled : CameraUiSideEffect

    data object FlashDisabled : CameraUiSideEffect
}
