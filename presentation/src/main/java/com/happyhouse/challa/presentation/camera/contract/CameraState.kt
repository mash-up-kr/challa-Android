package com.happyhouse.challa.presentation.camera.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import com.happyhouse.challa.presentation.model.ROOM_REQUIRED_PHOTO_COUNT

@Immutable
data class CameraState(
    val roomId: Long = 0L,
    val roomName: String = "해피하우스강릉여행",
    val lensFacing: CameraLensFacing = CameraLensFacing.BACK,
    val isFlashOn: Boolean = false,
    val hasFlashUnit: Boolean = false,
    val selectedFilterIndex: Int = 2,
    val remainingCount: Int = 12,
    val totalCount: Int = ROOM_REQUIRED_PHOTO_COUNT,
) : UiState

enum class CameraLensFacing {
    BACK,
    FRONT,
}
