package com.happyhouse.challa.presentation.camera.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface CameraSideEffect : UiSideEffect {
    /** 사용자가 [roomId]에 사진 촬영을 요청했음을 전달합니다. */
    data class PhotoCaptureRequested(
        val roomId: Long,
    ) : CameraSideEffect

    data object PhotoCaptureFailed : CameraSideEffect

    data object FlashNotAvailable : CameraSideEffect
}
