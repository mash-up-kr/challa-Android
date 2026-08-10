package com.happyhouse.challa.presentation.camera.model

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.domain.model.ShootableRoom

@Immutable
data class CameraRoomUiModel(
    val id: Long,
    val name: String,
    val remainingCount: Int,
    val totalCount: Int,
)

internal fun ShootableRoom.toUiModel(): CameraRoomUiModel =
    CameraRoomUiModel(
        id = id,
        name = title,
        remainingCount = remainingCount,
        totalCount = totalCount,
    )
