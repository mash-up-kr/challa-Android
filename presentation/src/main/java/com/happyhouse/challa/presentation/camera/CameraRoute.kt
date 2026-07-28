package com.happyhouse.challa.presentation.camera

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.camera.contract.CameraSideEffect
import com.happyhouse.challa.presentation.camera.permission.rememberCameraPermissionController
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarContent
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarVisuals
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaToastVisuals
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import kotlinx.coroutines.launch

@Composable
fun CameraRoute(
    roomId: Long,
    viewModel: CameraViewModel =
        hiltViewModel<CameraViewModel, CameraViewModel.Factory>(
            creationCallback = { factory ->
                factory.create(
                    roomId,
                )
            },
        ),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val permissionController = rememberCameraPermissionController()
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    var cameraBindingRetryKey by remember { mutableIntStateOf(0) }
    val roomLoadFailedMessage = stringResource(R.string.camera_room_load_failed_message)
    val flashNotAvailableMessage = stringResource(R.string.camera_flash_not_available_message)
    val photoCaptureFailedMessage = stringResource(R.string.camera_photo_capture_failed_message)
    val noRemainingCapturesMessage = stringResource(R.string.camera_no_remaining_captures_message)
    val cameraBindingFailedMessage = stringResource(R.string.camera_binding_failed_message)
    val retryLabel = stringResource(R.string.camera_retry)
    val destructiveIconTint = ChallaTheme.colors.statusDestructive

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                CameraSideEffect.RoomLoadFailed -> {
                    launch {
                        launch {
                            snackbarHostState.showSnackbar(
                                ChallaSnackbarVisuals(
                                    content =
                                        ChallaSnackbarContent.HeadingOnly(
                                            heading = roomLoadFailedMessage,
                                        ),
                                ),
                            )
                        }
                    }
                }

                CameraSideEffect.PhotoCaptureFailed -> {
                    launch {
                        snackbarHostState.showSnackbar(
                            ChallaToastVisuals(
                                message = photoCaptureFailedMessage,
                                icon = ChallaIcons.Error,
                                iconTint = destructiveIconTint,
                                topOffset = 112.dp,
                            ),
                        )
                    }
                }

                CameraSideEffect.FlashNotAvailable -> {
                    launch {
                        snackbarHostState.showSnackbar(
                            ChallaSnackbarVisuals(
                                content =
                                    ChallaSnackbarContent.HeadingOnly(
                                        heading = flashNotAvailableMessage,
                                    ),
                            ),
                        )
                    }
                }

                CameraSideEffect.NoRemainingCaptures -> {
                    launch {
                        snackbarHostState.showSnackbar(
                            ChallaToastVisuals(
                                message = noRemainingCapturesMessage,
                                icon = ChallaIcons.Error,
                                iconTint = destructiveIconTint,
                                topOffset = 112.dp,
                            ),
                        )
                    }
                }
            }
        }
    }

    CameraScreen(
        modifier = Modifier.fillMaxSize(),
        state = state.value,
        permissionState = permissionController.state,
        snackbarHostState = snackbarHostState,
        cameraBindingRetryKey = cameraBindingRetryKey,
        onRequestPermissionClick = permissionController.requestPermission,
        onCameraBindingFailed = { _ ->
            coroutineScope.launch {
                val result =
                    snackbarHostState.showSnackbar(
                        message = cameraBindingFailedMessage,
                        actionLabel = retryLabel,
                    )
                if (result == SnackbarResult.ActionPerformed) {
                    cameraBindingRetryKey += 1
                }
            }
        },
        onPhotoCaptureResult = viewModel::onPhotoCaptureResult,
        onPhotoCaptureCancelled = viewModel::onPhotoCaptureCancelled,
        onIntent = viewModel::onIntent,
    )
}
