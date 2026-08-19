package com.happyhouse.challa.presentation.camera.camerax

/** CameraX 세션에서 발생해 상위 UI가 한 번만 소비하는 이벤트입니다. */
internal sealed interface CameraSessionEvent {
    /** Controller 초기화 또는 Lifecycle 바인딩 실패를 전달합니다. */
    data object BindingFailed : CameraSessionEvent

    /** 카메라가 요청한 프레임의 노출을 시작했음을 전달합니다. */
    data class CaptureStarted(
        val requestId: Long,
    ) : CameraSessionEvent

    /** 촬영 요청 처리가 완료됐음을 전달합니다. */
    data class CaptureCompleted(
        val requestId: Long,
        val result: CameraCaptureResult,
    ) : CameraSessionEvent
}

/** CameraX 촬영 요청의 최종 처리 결과입니다. */
internal sealed interface CameraCaptureResult {
    /** CameraX가 업로드 가능한 JPEG 이미지를 정상적으로 캡처했습니다. */
    class Success(
        val imageBytes: ByteArray,
    ) : CameraCaptureResult

    /** CameraX가 촬영 요청을 완료하지 못했습니다. */
    data object Failed : CameraCaptureResult

    /** 세션 해제 등으로 진행 중인 촬영 코루틴이 취소됐습니다. */
    data object Cancelled : CameraCaptureResult
}
