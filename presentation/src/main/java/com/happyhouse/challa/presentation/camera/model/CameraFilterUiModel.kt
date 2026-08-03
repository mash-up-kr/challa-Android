package com.happyhouse.challa.presentation.camera.model

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.domain.model.CameraFilter

@Immutable
data class CameraFilterUiModel(
    val name: String,
    val fileUrl: String,
)

internal fun CameraFilter.toUiModel(): CameraFilterUiModel =
    CameraFilterUiModel(
        name = name,
        fileUrl = fileUrl,
    )
