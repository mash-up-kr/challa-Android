package com.happyhouse.challa.presentation.camera.model

import androidx.compose.runtime.Immutable

@Immutable
data class CameraRoomUiModel(
    val id: Long,
    val name: String,
    val remainingCount: Int,
    val totalCount: Int,
)
