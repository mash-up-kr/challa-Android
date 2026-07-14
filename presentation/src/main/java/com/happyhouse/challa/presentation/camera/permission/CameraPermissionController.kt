package com.happyhouse.challa.presentation.camera.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Composable
fun rememberCameraPermissionController(): CameraPermissionController {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var permissionState by remember { mutableStateOf<CameraPermissionState>(CameraPermissionState.Unchecked) }
    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }
    val settingsLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) {
            permissionState =
                if (context.hasCameraPermission()) {
                    CameraPermissionState.Granted
                } else {
                    CameraPermissionState.PermanentlyDenied
                }
        }
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            permissionState =
                if (isGranted) {
                    CameraPermissionState.Granted
                } else if (activity.canRequestCameraPermissionAgain()) {
                    CameraPermissionState.NotGranted
                } else {
                    CameraPermissionState.PermanentlyDenied
                }
        }

    LaunchedEffect(context) {
        if (context.hasCameraPermission()) {
            permissionState = CameraPermissionState.Granted
        } else {
            permissionState = CameraPermissionState.NotGranted
            hasRequestedPermission = true
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    return CameraPermissionController(
        state = permissionState,
        requestPermission = {
            val cannotRequestPermissionAgain =
                hasRequestedPermission && !activity.canRequestCameraPermissionAgain()

            if (permissionState == CameraPermissionState.PermanentlyDenied || cannotRequestPermissionAgain) {
                settingsLauncher.launch(context.cameraPermissionSettingsIntent())
            } else {
                hasRequestedPermission = true
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
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

private fun Activity?.canRequestCameraPermissionAgain(): Boolean =
    this != null &&
        ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.CAMERA,
        )

private fun Context.cameraPermissionSettingsIntent(): Intent =
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null),
    )
