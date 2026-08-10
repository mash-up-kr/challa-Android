package com.happyhouse.challa.presentation.camera.model

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.domain.model.CameraFilter

@Immutable
sealed interface CameraFilterUiModel {
    val name: String

    @Immutable
    data object Original : CameraFilterUiModel {
        override val name: String = "original"
    }

    @Immutable
    data class Remote(
        override val name: String,
        val fileUrl: String,
    ) : CameraFilterUiModel
}

internal fun CameraFilter.toUiModel(): CameraFilterUiModel =
    CameraFilterUiModel.Remote(
        name = name,
        fileUrl = fileUrl,
    )
