package com.happyhouse.challa.presentation.camera.model

import androidx.compose.runtime.Immutable

/**
 * 카메라 화면이 촬영부터 사진 생성 완료까지 유지하는 일회성 요청입니다.
 *
 * @property requestId 새 촬영 요청을 이전 요청과 구분하는 식별 토큰
 * @property selectedFilter 셔터를 누른 순간 선택되어 있던 필터
 * @property lensFacing 셔터를 누른 순간 선택되어 있던 렌즈 방향
 */
@Immutable
data class PhotoCaptureRequest(
    val requestId: Long,
    val roomId: Long,
    val selectedFilter: CameraFilterUiModel,
    val lensFacing: CameraLensFacing,
)
