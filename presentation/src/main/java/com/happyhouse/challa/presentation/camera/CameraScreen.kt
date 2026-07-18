package com.happyhouse.challa.presentation.camera

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.happyhouse.challa.presentation.camera.camerax.CameraBindingFailure
import com.happyhouse.challa.presentation.camera.component.CameraBackgroundTopColor
import com.happyhouse.challa.presentation.camera.contract.CameraIntent
import com.happyhouse.challa.presentation.camera.contract.CameraState
import com.happyhouse.challa.presentation.camera.model.PhotoCaptureRequest
import com.happyhouse.challa.presentation.camera.permission.CameraPermissionState

@Composable
fun CameraScreen(
    state: CameraState,
    permissionState: CameraPermissionState,
    snackbarHostState: SnackbarHostState,
    captureRequest: PhotoCaptureRequest?,
    cameraBindingRetryKey: Int,
    modifier: Modifier = Modifier,
    onRequestPermissionClick: () -> Unit,
    onCameraBindingFailed: (CameraBindingFailure) -> Unit,
    onPhotoCaptureResult: (requestId: Long, roomId: Long, succeeded: Boolean) -> Unit,
    onPhotoCaptureCancelled: (requestId: Long) -> Unit,
    onIntent: (CameraIntent) -> Unit,
) {
    val activity = LocalActivity.current as? ComponentActivity

    DisposableEffect(activity) {
        activity?.enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.dark(
                    scrim = CameraBackgroundTopColor.toArgb(),
                ),
            navigationBarStyle =
                SystemBarStyle.dark(
                    scrim = Color.Black.toArgb(),
                ),
        )

        onDispose {
            activity?.enableEdgeToEdge(
                statusBarStyle =
                    SystemBarStyle.light(
                        scrim = Color.Transparent.toArgb(),
                        darkScrim = Color.Transparent.toArgb(),
                    ),
                navigationBarStyle =
                    SystemBarStyle.light(
                        scrim = Color.Transparent.toArgb(),
                        darkScrim = Color.Transparent.toArgb(),
                    ),
            )
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = CameraBackgroundTopColor,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { innerPadding ->
        CameraContent(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            state = state,
            permissionState = permissionState,
            captureRequest = captureRequest,
            cameraBindingRetryKey = cameraBindingRetryKey,
            onRequestPermissionClick = onRequestPermissionClick,
            onCameraBindingFailed = onCameraBindingFailed,
            onPhotoCaptureResult = onPhotoCaptureResult,
            onPhotoCaptureCancelled = onPhotoCaptureCancelled,
            onIntent = onIntent,
        )
    }
}
