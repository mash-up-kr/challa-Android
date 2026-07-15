package com.happyhouse.challa.presentation.camera.model

import androidx.compose.runtime.Immutable

/**
 * CameraX 세션에 전달되는 일회성 촬영 요청입니다.
 *
 * 동일한 방을 연속 촬영해도 새로운 요청임을 구분할 수 있도록 [requestId]를 매번 증가시킵니다.
 * 촬영 성공 후 감소시킬 장수는 [roomId]에 해당하는 방을 기준으로 결정합니다.
 * 처리 결과를 받은 호출자는 세션 재진입 시 같은 요청이 다시 처리되지 않도록 요청을 제거해야 합니다.
 */
@Immutable
data class PhotoCaptureRequest(
    val requestId: Long,
    val roomId: Long,
)
