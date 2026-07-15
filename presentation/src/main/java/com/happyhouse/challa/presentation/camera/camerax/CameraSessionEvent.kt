package com.happyhouse.challa.presentation.camera.camerax

/**
 * CameraX 세션에서 발생해 상위 UI로 전달되는 이벤트입니다.
 *
 * 카메라 준비·촬영 상태와 사진 촬영 결과, 플래시 지원 및 활성 상태 변화를 전달합니다.
 */
internal sealed interface CameraSessionEvent {
    /**
     * 카메라 준비 또는 촬영 상태가 변경됐음을 전달합니다.
     *
     * 세션이 해제되면 기본 [CameraSessionState]가 전달됩니다.
     */
    data class StateChanged(
        val state: CameraSessionState,
    ) : CameraSessionEvent

    /** 카메라가 촬영할 프레임의 노출을 시작했음을 전달합니다. */
    data object CaptureStarted : CameraSessionEvent

    /**
     * 사진 촬영이 성공하거나 오류로 종료됐음을 전달합니다.
     *
     * 진행 중인 촬영 코루틴이 취소된 경우에는 [PhotoCaptureCancelled]가 전달됩니다.
     *
     * @property roomId 촬영 대상 방 ID
     * @property succeeded 사진 촬영 성공 여부
     */
    data class PhotoCaptureResult(
        val roomId: Long,
        val succeeded: Boolean,
    ) : CameraSessionEvent

    /**
     * 진행 중인 촬영 코루틴이 취소됐음을 전달합니다.
     *
     * @property roomId 취소된 촬영의 대상 방 ID
     */
    data class PhotoCaptureCancelled(
        val roomId: Long,
    ) : CameraSessionEvent

    /**
     * 현재 바인딩된 렌즈가 플래시 하드웨어를 지원하는지 전달합니다.
     *
     * 세션이 해제되면 [isAvailable]은 false로 전달됩니다.
     *
     * @property isAvailable 플래시 지원 여부
     */
    data class FlashAvailabilityChanged(
        val isAvailable: Boolean,
    ) : CameraSessionEvent

    /**
     * 토치 제어가 완료된 뒤 UI에 반영할 플래시 활성 상태를 전달합니다.
     *
     * @property isEnabled 플래시 활성화 여부
     */
    data class FlashStateChanged(
        val isEnabled: Boolean,
    ) : CameraSessionEvent
}
