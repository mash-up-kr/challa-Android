package com.happyhouse.challa.presentation.camera.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface CameraSideEffect : UiSideEffect {
    data object PhotoCaptureFailed : CameraSideEffect

    data object FlashNotAvailable : CameraSideEffect
}
