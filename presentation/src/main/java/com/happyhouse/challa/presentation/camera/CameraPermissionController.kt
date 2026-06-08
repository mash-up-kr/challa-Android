package com.happyhouse.challa.presentation.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun rememberCameraPermissionController(): CameraPermissionController {
    val context = LocalContext.current
    var permissionState by remember { mutableStateOf<CameraPermissionState>(CameraPermissionState.Checking) }
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            permissionState =
                if (isGranted) {
                    CameraPermissionState.Granted
                } else {
                    CameraPermissionState.Denied
                }
        }

    LaunchedEffect(context) {
        if (context.hasCameraPermission()) {
            permissionState = CameraPermissionState.Granted
        } else {
            permissionState = CameraPermissionState.Denied
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    return CameraPermissionController(
        state = permissionState,
        requestPermission = {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        },
    )
}

data class CameraPermissionController(
    val state: CameraPermissionState,
    val requestPermission: () -> Unit,
)

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA,
    ) == PackageManager.PERMISSION_GRANTED
