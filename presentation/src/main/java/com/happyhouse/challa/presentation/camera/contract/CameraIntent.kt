package com.happyhouse.challa.presentation.camera.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface CameraIntent : UiIntent {
    data class FetchData(
        val roomId: Long,
    ) : CameraIntent

    data object FlashClick : CameraIntent

    data object SwitchCameraClick : CameraIntent

    data object ShutterClick : CameraIntent

    data class FlashAvailabilityChanged(
        val isAvailable: Boolean,
    ) : CameraIntent
}
