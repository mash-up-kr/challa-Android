package com.happyhouse.challa.presentation.camera.camerax

/**
 * CameraX 세션에서 발생해 상위 UI로 전달되는 이벤트입니다.
 *
 * 카메라 준비·촬영 상태와 바인딩 실패, 사진 촬영 결과, 플래시 지원 여부를 전달합니다.
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

    /** CameraX UseCase를 Lifecycle에 바인딩하지 못했음을 전달합니다. */
    data object BindingFailed : CameraSessionEvent

    /** 카메라가 촬영할 프레임의 노출을 시작했음을 전달합니다. */
    data object CaptureStarted : CameraSessionEvent

    /**
     * CameraX 촬영 성공 여부를 전달합니다.
     *
     * 진행 중인 촬영 코루틴이 취소된 경우에는 [PhotoCaptureCancelled]가 전달됩니다.
     * [succeeded]는 CameraX가 이미지를 정상적으로 캡처했는지만 나타내며, 파일 저장이나 서버 업로드 성공을 의미하지 않습니다.
     */
    data class PhotoCaptureResult(
        val roomId: Long,
        val succeeded: Boolean,
    ) : CameraSessionEvent

    /** 진행 중인 촬영 코루틴이 취소됐음을 전달합니다. */
    data class PhotoCaptureCancelled(
        val roomId: Long,
    ) : CameraSessionEvent

    /**
     * 현재 바인딩된 렌즈가 플래시 하드웨어를 지원하는지 전달합니다.
     *
     * 세션이 해제되면 [isAvailable]은 false로 전달됩니다.
     */
    data class FlashAvailabilityChanged(
        val isAvailable: Boolean,
    ) : CameraSessionEvent
}
