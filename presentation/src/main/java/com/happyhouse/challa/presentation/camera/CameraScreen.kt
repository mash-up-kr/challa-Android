package com.happyhouse.challa.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.happyhouse.challa.presentation.camera.component.CameraContent
import com.happyhouse.challa.presentation.camera.component.CameraPermissionDeniedContent
import com.happyhouse.challa.presentation.camera.contract.CameraUiIntent
import com.happyhouse.challa.presentation.camera.contract.CameraUiState
import com.happyhouse.challa.presentation.camera.permission.CameraPermissionState

@Composable
fun CameraScreen(
    state: CameraUiState,
    permissionState: CameraPermissionState,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onRequestPermissionClick: () -> Unit,
    onIntent: (CameraUiIntent) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            when (permissionState) {
                CameraPermissionState.Checking -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                CameraPermissionState.Granted -> {
                    CameraContent(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .systemBarsPadding(),
                        state = state,
                        onBackClick = onBackClick,
                        onIntent = onIntent,
                    )
                }

                CameraPermissionState.Denied -> {
                    CameraPermissionDeniedContent(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                        onBackClick = onBackClick,
                        onRequestPermissionClick = onRequestPermissionClick,
                    )
                }
            }
        }
    }
}
