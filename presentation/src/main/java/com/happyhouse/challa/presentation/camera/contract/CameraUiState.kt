package com.happyhouse.challa.presentation.camera.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState

@Immutable
data class CameraUiState(
    val roomId: Long = 0L,
    val lensFacing: CameraLensFacing = CameraLensFacing.BACK,
    val isFlashOn: Boolean = false,
    val hasFlashUnit: Boolean = false,
    val remainingCount: Int = 12,
    val totalCount: Int = 24,
) : UiState

enum class CameraLensFacing {
    BACK,
    FRONT,
}
