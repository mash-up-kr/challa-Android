package com.happyhouse.challa.presentation.camera

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.camera.contract.CameraIntent
import com.happyhouse.challa.presentation.camera.contract.CameraOnboardingState
import com.happyhouse.challa.presentation.camera.contract.CameraRoomLoadState
import com.happyhouse.challa.presentation.camera.contract.CameraSideEffect
import com.happyhouse.challa.presentation.camera.onboarding.rememberCameraOnboardingVisibility
import com.happyhouse.challa.presentation.camera.permission.CameraPermissionState
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
    val feedbackSnackbarHostState = remember { SnackbarHostState() }
    val onboardingSnackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val permissionController = rememberCameraPermissionController()
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    val roomLoadFailedMessage = stringResource(R.string.camera_room_load_failed_message)
    val filterListLoadFailedMessage = stringResource(R.string.camera_filter_list_load_failed_message)
    val filterLoadFailedMessage = stringResource(R.string.camera_filter_load_failed_message)
    val flashNotAvailableMessage = stringResource(R.string.camera_flash_not_available_message)
    val photoCaptureFailedMessage = stringResource(R.string.camera_photo_capture_failed_message)
    val noRemainingCapturesMessage = stringResource(R.string.camera_no_remaining_captures_message)
    val cameraBindingFailedMessage = stringResource(R.string.camera_binding_failed_message)
    val retryLabel = stringResource(R.string.camera_retry)
    val destructiveIconTint = ChallaTheme.colors.statusDestructive
    val shouldShowOnboarding =
        state.value.onboardingState == CameraOnboardingState.REQUIRED &&
            state.value.roomLoadState == CameraRoomLoadState.LOADED &&
            permissionController.state == CameraPermissionState.Granted
    val isOnboardingVisible =
        rememberCameraOnboardingVisibility(
            shouldShow = shouldShowOnboarding,
            snackbarHostState = onboardingSnackbarHostState,
            onCompleted = { viewModel.onIntent(CameraIntent.OnboardingConfirmClick) },
        )

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                CameraSideEffect.RoomLoadFailed -> {
                    launch {
                        val result =
                            feedbackSnackbarHostState.showSnackbar(
                                ChallaSnackbarVisuals(
                                    content =
                                        ChallaSnackbarContent.HeadingOnly(
                                            heading = roomLoadFailedMessage,
                                        ),
                                    actionLabel = retryLabel,
                                ),
                            )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.onIntent(CameraIntent.RoomLoadRetry)
                        }
                    }
                }

                CameraSideEffect.FilterListLoadFailed -> {
                    launch {
                        val result =
                            feedbackSnackbarHostState.showSnackbar(
                                ChallaSnackbarVisuals(
                                    content =
                                        ChallaSnackbarContent.HeadingOnly(
                                            heading = filterListLoadFailedMessage,
                                        ),
                                    actionLabel = retryLabel,
                                ),
                            )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.onIntent(CameraIntent.FilterListLoadRetry)
                        }
                    }
                }

                CameraSideEffect.SelectedFilterLutLoadFailed -> {
                    launch {
                        feedbackSnackbarHostState.showSnackbar(
                            ChallaToastVisuals(
                                message = filterLoadFailedMessage,
                                icon = ChallaIcons.Error,
                                iconTint = destructiveIconTint,
                                topOffset = 112.dp,
                            ),
                        )
                    }
                }

                CameraSideEffect.PhotoCaptureFailed -> {
                    launch {
                        feedbackSnackbarHostState.showSnackbar(
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
                        feedbackSnackbarHostState.showSnackbar(
                            ChallaToastVisuals(
                                message = flashNotAvailableMessage,
                                icon = ChallaIcons.Error,
                                iconTint = destructiveIconTint,
                                topOffset = 112.dp,
                            ),
                        )
                    }
                }

                CameraSideEffect.NoRemainingCaptures -> {
                    launch {
                        feedbackSnackbarHostState.showSnackbar(
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
        feedbackSnackbarHostState = feedbackSnackbarHostState,
        onboardingSnackbarHostState = onboardingSnackbarHostState,
        isOnboardingVisible = isOnboardingVisible,
        onRequestPermissionClick = permissionController.requestPermission,
        onCameraBindingFailed = {
            coroutineScope.launch {
                feedbackSnackbarHostState.showSnackbar(
                    ChallaToastVisuals(
                        message = cameraBindingFailedMessage,
                        icon = ChallaIcons.Error,
                        iconTint = destructiveIconTint,
                        topOffset = 112.dp,
                    ),
                )
            }
        },
        onPhotoCaptured = viewModel::onPhotoCaptured,
        onPhotoCaptureFailed = viewModel::onPhotoCaptureFailed,
        onPhotoCaptureCancelled = viewModel::onPhotoCaptureCancelled,
        getCameraFilterFile = viewModel::getCameraFilterFile,
        onIntent = viewModel::onIntent,
    )
}
