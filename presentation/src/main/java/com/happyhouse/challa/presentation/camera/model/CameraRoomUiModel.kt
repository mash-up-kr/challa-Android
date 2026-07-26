package com.happyhouse.challa.presentation.camera.model

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.domain.model.RoomSummary

@Immutable
data class CameraRoomUiModel(
    val id: Long,
    val name: String,
    val remainingCount: Int,
    val totalCount: Int,
)

internal fun RoomSummary.toUiModel(): CameraRoomUiModel =
    CameraRoomUiModel(
        id = id,
        name = name,
        remainingCount = remainingCount,
        totalCount = totalCount,
    )
