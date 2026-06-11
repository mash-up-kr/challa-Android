package com.happyhouse.challa.presentation.camera

import androidx.compose.runtime.Composable
import com.happyhouse.challa.presentation.camera.permission.rememberCameraPermissionController

@Composable
fun CameraRoute(
    roomId: Long,
    onBackClick: () -> Unit,
) {
    val permissionController = rememberCameraPermissionController()

    CameraScreen(
        permissionState = permissionController.state,
        onRequestPermissionClick = permissionController.requestPermission,
        onBackClick = onBackClick,
    )
}
