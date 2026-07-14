package com.happyhouse.challa.presentation.camera.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface CameraIntent : UiIntent {
    data class FetchData(
        val roomId: Long,
    ) : CameraIntent

    data object FlashClick : CameraIntent

    data object SwitchCameraClick : CameraIntent

    data class ShutterClick(
        val roomId: Long,
    ) : CameraIntent

    data object ZoomClick : CameraIntent

    data class RoomClick(
        val roomId: Long,
    ) : CameraIntent

    data class FilterClick(
        val index: Int,
    ) : CameraIntent

    data class FlashAvailabilityChanged(
        val isAvailable: Boolean,
    ) : CameraIntent
}
