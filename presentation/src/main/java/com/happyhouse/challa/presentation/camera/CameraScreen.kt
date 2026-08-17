package com.happyhouse.challa.presentation.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.happyhouse.challa.presentation.camera.contract.CameraIntent
import com.happyhouse.challa.presentation.camera.contract.CameraState
import com.happyhouse.challa.presentation.camera.permission.CameraPermissionState
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarHost
import com.happyhouse.challa.presentation.designsystem.layout.ChallaScaffold
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

@Composable
fun CameraScreen(
    state: CameraState,
    permissionState: CameraPermissionState,
    feedbackSnackbarHostState: SnackbarHostState,
    onboardingSnackbarHostState: SnackbarHostState,
    isOnboardingVisible: Boolean,
    modifier: Modifier = Modifier,
    onRequestPermissionClick: () -> Unit,
    onCameraBindingFailed: () -> Unit,
    onPhotoCaptured: (requestId: Long, imageBytes: ByteArray) -> Unit,
    onPhotoCaptureFailed: (requestId: Long) -> Unit,
    onPhotoCaptureCancelled: (requestId: Long) -> Unit,
    getCameraFilterFile: suspend (String) -> ByteArray?,
    onIntent: (CameraIntent) -> Unit,
) {
    val cameraBackgroundColor = ChallaTheme.colors.staticBlack.copy(alpha = 0.9f)

    Box(modifier = modifier.fillMaxSize()) {
        ChallaScaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = cameraBackgroundColor,
            snackbarHostState = onboardingSnackbarHostState,
        ) { innerPadding ->
            CameraContent(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                state = state,
                permissionState = permissionState,
                isOnboardingVisible = isOnboardingVisible,
                onRequestPermissionClick = onRequestPermissionClick,
                onCameraBindingFailed = onCameraBindingFailed,
                onPhotoCaptured = onPhotoCaptured,
                onPhotoCaptureFailed = onPhotoCaptureFailed,
                onPhotoCaptureCancelled = onPhotoCaptureCancelled,
                getCameraFilterFile = getCameraFilterFile,
                onIntent = onIntent,
            )
        }

        ChallaSnackbarHost(
            hostState = feedbackSnackbarHostState,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
