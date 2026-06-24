package com.happyhouse.challa.presentation.camera.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import com.happyhouse.challa.presentation.model.ROOM_REQUIRED_PHOTO_COUNT

@Immutable
data class CameraState(
    val roomId: Long = 0L,
    val lensFacing: CameraLensFacing = CameraLensFacing.BACK,
    val isFlashOn: Boolean = false,
    val hasFlashUnit: Boolean = false,
    val remainingCount: Int = 12,
    val totalCount: Int = ROOM_REQUIRED_PHOTO_COUNT,
) : UiState

enum class CameraLensFacing {
    BACK,
    FRONT,
}
