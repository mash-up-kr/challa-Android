package com.happyhouse.challa.presentation.camera.camerax

import androidx.camera.core.ImageProxy
import com.happyhouse.challa.presentation.camera.model.PhotoCaptureRequest

/**
 * CameraX가 촬영한 이미지와 촬영 순간의 설정을 함께 처리합니다.
 *
 * [process]가 [ImageProxy]의 소유권을 넘겨받아 항상 닫습니다. 향후 회전·전면 렌즈 반전,
 * [PhotoCaptureRequest.selectedFilter] 적용 및 저장·업로드 처리는 이 경계 안에 구현합니다.
 */
internal class CapturedImageProcessor {
    suspend fun process(
        image: ImageProxy,
        request: PhotoCaptureRequest,
    ) {
        image.use {
            // TODO: request에 고정된 렌즈와 필터로 이미지를 변환하고 저장·업로드합니다.
        }
    }
}
