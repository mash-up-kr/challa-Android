package com.happyhouse.challa.presentation.camera

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import com.happyhouse.challa.presentation.camera.camerax.CameraBindingFailure
import com.happyhouse.challa.presentation.camera.contract.CameraIntent
import com.happyhouse.challa.presentation.camera.contract.CameraState
import com.happyhouse.challa.presentation.camera.permission.CameraPermissionState
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarHost
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

@Composable
fun CameraScreen(
    state: CameraState,
    permissionState: CameraPermissionState,
    snackbarHostState: SnackbarHostState,
    cameraBindingRetryKey: Int,
    modifier: Modifier = Modifier,
    onRequestPermissionClick: () -> Unit,
    onCameraBindingFailed: (CameraBindingFailure) -> Unit,
    onPhotoCaptureResult: (requestId: Long, succeeded: Boolean) -> Unit,
    onPhotoCaptureCancelled: (requestId: Long) -> Unit,
    onIntent: (CameraIntent) -> Unit,
) {
    val activity = LocalActivity.current as? ComponentActivity
    val cameraBackgroundColor =
        ChallaTheme.colors.staticBlack
            .copy(alpha = 0.9f)
            .compositeOver(ChallaTheme.colors.staticWhite)

    DisposableEffect(activity) {
        activity?.enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.dark(
                    scrim = cameraBackgroundColor.toArgb(),
                ),
            navigationBarStyle =
                SystemBarStyle.dark(
                    scrim = cameraBackgroundColor.toArgb(),
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

    Box(modifier = modifier) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = cameraBackgroundColor,
        ) { innerPadding ->
            CameraContent(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                state = state,
                permissionState = permissionState,
                cameraBindingRetryKey = cameraBindingRetryKey,
                onRequestPermissionClick = onRequestPermissionClick,
                onCameraBindingFailed = onCameraBindingFailed,
                onPhotoCaptureResult = onPhotoCaptureResult,
                onPhotoCaptureCancelled = onPhotoCaptureCancelled,
                onIntent = onIntent,
            )
        }

        ChallaSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
