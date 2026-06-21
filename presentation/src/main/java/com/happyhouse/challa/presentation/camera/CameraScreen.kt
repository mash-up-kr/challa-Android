package com.happyhouse.challa.presentation.camera

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.happyhouse.challa.presentation.camera.component.CameraContent
import com.happyhouse.challa.presentation.camera.component.CameraPermissionDeniedContent
import com.happyhouse.challa.presentation.camera.contract.CameraIntent
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
    onIntent: (CameraIntent) -> Unit,
) {
    val activity = LocalActivity.current as? ComponentActivity

    DisposableEffect(activity) {
        activity?.enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.dark(
                    scrim = Color.Transparent.toArgb(),
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
        containerColor = Color.Black,
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
                CameraPermissionState.Unchecked -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                CameraPermissionState.Granted -> {
                    CameraContent(
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        onBackClick = onBackClick,
                        onIntent = onIntent,
                    )
                }

                CameraPermissionState.NotGranted -> {
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
