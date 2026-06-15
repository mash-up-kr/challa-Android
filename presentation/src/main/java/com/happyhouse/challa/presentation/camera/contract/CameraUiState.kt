package com.happyhouse.challa.presentation.camera.contract

import androidx.camera.core.CameraSelector
import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState

@Immutable
data class CameraUiState(
    val roomId: Long = 0L,
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    val isFlashOn: Boolean = false,
    val hasFlashUnit: Boolean = false,
    val remainingCount: Int = 12,
    val totalCount: Int = 24,
) : UiState
