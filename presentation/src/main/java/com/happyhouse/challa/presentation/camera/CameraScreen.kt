package com.happyhouse.challa.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.camera.component.CameraContent
import com.happyhouse.challa.presentation.camera.component.CameraContentLayout
import com.happyhouse.challa.presentation.camera.component.CameraTopBar
import com.happyhouse.challa.presentation.camera.component.MockViewFinder
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

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
                CameraTopBar(
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .systemBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                    onBackClick = onBackClick,
                    remainingCount = 24,
                    totalCount = 24,
                    isFlashOn = false,
                    isFlashEnabled = false,
                    onFlashClick = {},
                )
                CameraPermissionGuide(
                    modifier = Modifier.align(Alignment.Center),
                    onRequestPermissionClick = onRequestPermissionClick,
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionGuide(
    modifier: Modifier = Modifier,
    onRequestPermissionClick: () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.camera_permission_required_title),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.camera_permission_required_description),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            modifier = Modifier.padding(top = 24.dp),
            onClick = onRequestPermissionClick,
        ) {
            Text(text = stringResource(R.string.camera_permission_request_button))
        }
    }
}

@ComposePreview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CameraScreenDeniedPreview() {
    CameraScreen(
        permissionState = CameraPermissionState.Denied,
        onRequestPermissionClick = {},
        onBackClick = {},
    )
}

@ComposePreview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CameraScreenGrantedPreview() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black),
    ) {
        CameraContentLayout(
            modifier = Modifier.fillMaxSize(),
            isFlashOn = false,
            isFlashEnabled = true,
            onBackClick = {},
            onFlashClick = {},
            onSwitchCameraClick = {},
            onShutterClick = {},
            viewFinder = { modifier ->
                MockViewFinder(modifier = modifier)
            },
        )
    }
}
