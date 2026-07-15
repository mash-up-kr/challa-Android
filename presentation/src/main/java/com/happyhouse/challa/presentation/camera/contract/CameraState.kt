package com.happyhouse.challa.presentation.camera.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import com.happyhouse.challa.presentation.camera.model.CameraRoomUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class CameraState(
    val selectedRoomId: Long = 0L,
    val lensFacing: CameraLensFacing = CameraLensFacing.BACK,
    val isFlashEnabled: Boolean = false,
    val hasFlashUnit: Boolean = false,
    val isCapturePending: Boolean = false,
    val zoomLevel: Int = 1,
    val filterCount: Int = 8,
    val selectedFilterIndex: Int = 0,
    val rooms: ImmutableList<CameraRoomUiModel> = persistentListOf(),
) : UiState {
    val selectedRoom: CameraRoomUiModel?
        get() = rooms.firstOrNull { it.id == selectedRoomId }
}

enum class CameraLensFacing {
    BACK,
    FRONT,
}
