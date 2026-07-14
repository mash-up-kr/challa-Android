package com.happyhouse.challa.presentation.camera.permission

import androidx.compose.runtime.Immutable

@Immutable
sealed interface CameraPermissionState {
    data object Unchecked : CameraPermissionState

    data object Granted : CameraPermissionState

    data object NotGranted : CameraPermissionState

    data object PermanentlyDenied : CameraPermissionState
}
