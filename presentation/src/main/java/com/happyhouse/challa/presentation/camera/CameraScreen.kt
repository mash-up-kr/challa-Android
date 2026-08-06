package com.happyhouse.challa.presentation.camera

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.happyhouse.challa.presentation.camera.camerax.CameraBindingFailure
import com.happyhouse.challa.presentation.camera.contract.CameraIntent
import com.happyhouse.challa.presentation.camera.contract.CameraState
import com.happyhouse.challa.presentation.camera.permission.CameraPermissionState
import com.happyhouse.challa.presentation.designsystem.layout.ChallaScaffold
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
    getCameraFilterFile: suspend (String) -> ByteArray?,
    onIntent: (CameraIntent) -> Unit,
) {
    val cameraBackgroundColor = ChallaTheme.colors.staticBlack.copy(alpha = 0.9f)

    ChallaScaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = cameraBackgroundColor,
        snackbarHostState = snackbarHostState,
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
            getCameraFilterFile = getCameraFilterFile,
            onIntent = onIntent,
        )
    }
}
