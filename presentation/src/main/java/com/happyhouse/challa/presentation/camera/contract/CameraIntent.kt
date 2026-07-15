package com.happyhouse.challa.presentation.camera.contract

import com.happyhouse.challa.presentation.base.UiIntent
import com.happyhouse.challa.presentation.camera.model.CameraRoomUiModel

/** 카메라 화면의 사용자 입력과 CameraX 세션에서 전달되는 상태 변화입니다. */
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
        val room: CameraRoomUiModel,
    ) : CameraIntent

    data class FilterClick(
        val index: Int,
    ) : CameraIntent

    /** 현재 렌즈의 플래시 지원 여부를 [isAvailable]로 갱신합니다. */
    data class FlashAvailabilityChanged(
        val isAvailable: Boolean,
    ) : CameraIntent
}
