package com.happyhouse.challa.presentation.camera.model

import androidx.compose.runtime.Immutable

/**
 * CameraX 세션에 전달되는 일회성 촬영 요청입니다.
 *
 * @property requestId 새 촬영 요청을 이전 요청과 구분하는 식별 토큰
 * @property roomId 촬영 대상 방 ID
 */
@Immutable
data class PhotoCaptureRequest(
    val requestId: Long,
    val roomId: Long,
)
