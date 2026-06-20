package com.happyhouse.challa.presentation.camera.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface CameraUiIntent : UiIntent {
    data class FetchData(
        val roomId: Long,
    ) : CameraUiIntent

    data object FlashClick : CameraUiIntent

    data object SwitchCameraClick : CameraUiIntent

    data object ShutterClick : CameraUiIntent

    data class FlashAvailabilityChanged(
        val isAvailable: Boolean,
    ) : CameraUiIntent
}
