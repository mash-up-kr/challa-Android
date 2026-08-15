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
    isOnboardingVisible: Boolean,
    cameraBindingRetryKey: Int,
    modifier: Modifier = Modifier,
    onRequestPermissionClick: () -> Unit,
    onCameraBindingFailed: (CameraBindingFailure) -> Unit,
    onPhotoCaptured: (requestId: Long, imageBytes: ByteArray) -> Unit,
    onPhotoCaptureFailed: (requestId: Long) -> Unit,
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
            isOnboardingVisible = isOnboardingVisible,
            cameraBindingRetryKey = cameraBindingRetryKey,
            onRequestPermissionClick = onRequestPermissionClick,
            onCameraBindingFailed = onCameraBindingFailed,
            onPhotoCaptured = onPhotoCaptured,
            onPhotoCaptureFailed = onPhotoCaptureFailed,
            onPhotoCaptureCancelled = onPhotoCaptureCancelled,
            getCameraFilterFile = getCameraFilterFile,
            onIntent = onIntent,
        )
    }
}
