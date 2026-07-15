package com.happyhouse.challa.presentation.camera

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.camera.contract.CameraSideEffect
import com.happyhouse.challa.presentation.camera.model.PhotoCaptureRequest
import com.happyhouse.challa.presentation.camera.permission.rememberCameraPermissionController
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
    val permissionController = rememberCameraPermissionController()
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    var captureRequest by remember { mutableStateOf<PhotoCaptureRequest?>(null) }
    val flashNotAvailableMessage = stringResource(R.string.camera_flash_not_available_message)
    val photoCaptureFailedMessage = stringResource(R.string.camera_photo_capture_failed_message)

    LaunchedEffect(viewModel) {
        var nextCaptureRequestId = 0L

        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is CameraSideEffect.PhotoCaptureRequested -> {
                    nextCaptureRequestId += 1
                    captureRequest =
                        PhotoCaptureRequest(
                            requestId = nextCaptureRequestId,
                            roomId = effect.roomId,
                        )
                }

                CameraSideEffect.PhotoCaptureFailed -> {
                    launch {
                        snackbarHostState.showSnackbar(photoCaptureFailedMessage)
                    }
                }

                CameraSideEffect.FlashNotAvailable -> {
                    launch {
                        snackbarHostState.showSnackbar(flashNotAvailableMessage)
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
        captureRequest = captureRequest,
        onRequestPermissionClick = permissionController.requestPermission,
        onPhotoCaptureResult = { roomId, succeeded ->
            captureRequest = null
            viewModel.onPhotoCaptureResult(roomId, succeeded)
        },
        onIntent = viewModel::onIntent,
    )
}
