package com.happyhouse.challa.presentation.camera.contract

import com.happyhouse.challa.presentation.base.UiIntent
import com.happyhouse.challa.presentation.camera.model.CameraRoomUiModel

/** 카메라 화면의 사용자 입력과 화면 동작 요청입니다. */
sealed interface CameraIntent : UiIntent {
    data class FetchData(
        val roomId: Long,
    ) : CameraIntent

    data object RoomLoadRetry : CameraIntent

    /**
     * 플래시 버튼을 누른 시점의 하드웨어 지원 여부와 함께 전달합니다.
     *
     * @property isAvailable 현재 CameraX 세션에 바인딩된 렌즈의 플래시 지원 여부
     */
    data class FlashClick(
        val isAvailable: Boolean,
    ) : CameraIntent

    data object SwitchCameraClick : CameraIntent

    data object ShutterClick : CameraIntent

    data object ZoomClick : CameraIntent

    data class RoomClick(
        val room: CameraRoomUiModel,
    ) : CameraIntent

    data class FilterClick(
        val index: Int,
    ) : CameraIntent
}
