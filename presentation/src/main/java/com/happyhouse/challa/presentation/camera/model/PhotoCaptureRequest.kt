package com.happyhouse.challa.presentation.camera.model

import androidx.compose.runtime.Immutable

@Immutable
data class PhotoCaptureRequest(
    val requestId: Long,
    val roomId: Long,
)
