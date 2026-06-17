package com.happyhouse.challa.presentation.camera

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.camera.contract.CameraUiSideEffect
import com.happyhouse.challa.presentation.camera.permission.rememberCameraPermissionController

@Composable
fun CameraRoute(
    roomId: Long,
    onBackClick: () -> Unit,
    viewModel: CameraViewModel =
        hiltViewModel<CameraViewModel, CameraViewModel.Factory>(
            creationCallback = { factory ->
                factory.create(
                    roomId,
                )
            },
        ),
) {
    val permissionController = rememberCameraPermissionController()
    val snackbarHostState = remember { SnackbarHostState() }
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    val flashNotAvailableMessage = stringResource(R.string.camera_flash_not_available_message)
    val flashEnabledMessage = stringResource(R.string.camera_flash_enabled_message)
    val flashDisabledMessage = stringResource(R.string.camera_flash_disabled_message)

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            val message =
                when (effect) {
                    CameraUiSideEffect.FlashNotAvailable -> flashNotAvailableMessage
                    CameraUiSideEffect.FlashEnabled -> flashEnabledMessage
                    CameraUiSideEffect.FlashDisabled -> flashDisabledMessage
                }

            snackbarHostState.showSnackbar(message)
        }
    }

    CameraScreen(
        modifier = Modifier.fillMaxSize(),
        state = state.value,
        permissionState = permissionController.state,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onRequestPermissionClick = permissionController.requestPermission,
        onIntent = viewModel::onIntent,
    )
}
