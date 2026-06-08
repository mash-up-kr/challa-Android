package com.happyhouse.challa.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.happyhouse.challa.presentation.camera.component.CameraContent
import com.happyhouse.challa.presentation.camera.component.CameraPermissionDeniedContent
import com.happyhouse.challa.presentation.camera.permission.CameraPermissionState

@Composable
fun CameraScreen(
    permissionState: CameraPermissionState,
    onRequestPermissionClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black),
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
                    onBackClick = onBackClick,
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
