package com.happyhouse.challa.presentation.camera

import androidx.compose.runtime.Immutable

@Immutable
sealed interface CameraPermissionState {
    data object Checking : CameraPermissionState

    data object Granted : CameraPermissionState

    data object Denied : CameraPermissionState
}
