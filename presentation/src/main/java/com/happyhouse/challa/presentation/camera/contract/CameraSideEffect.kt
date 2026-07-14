package com.happyhouse.challa.presentation.camera.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface CameraSideEffect : UiSideEffect {
    data class CapturePhoto(
        val roomId: Long,
    ) : CameraSideEffect

    data object PhotoCaptureFailed : CameraSideEffect

    data object FlashDisabled : CameraSideEffect

    data object FlashEnabled : CameraSideEffect

    data object FlashNotAvailable : CameraSideEffect
}
