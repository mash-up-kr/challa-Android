package com.happyhouse.challa.presentation.camera.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.camera.contract.CameraUiIntent
import com.happyhouse.challa.presentation.camera.contract.CameraUiState
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.model.ROOM_REQUIRED_PHOTO_COUNT
import androidx.camera.core.Camera as CameraXCamera
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
fun CameraContent(
    modifier: Modifier = Modifier,
    state: CameraUiState,
    onBackClick: () -> Unit,
    onIntent: (CameraUiIntent) -> Unit,
) {
    var camera by remember { mutableStateOf<CameraXCamera?>(null) }

    LaunchedEffect(camera, state.isFlashOn, state.hasFlashUnit) {
        camera?.cameraControl?.enableTorch(state.isFlashOn && state.hasFlashUnit)
    }

    CameraContentLayout(
        modifier = modifier,
        remainingCount = state.remainingCount,
        totalCount = state.totalCount,
        onBackClick = onBackClick,
        onFlashClick = { onIntent(CameraUiIntent.FlashClick) },
        onSwitchCameraClick = { onIntent(CameraUiIntent.SwitchCameraClick) },
        onShutterClick = { onIntent(CameraUiIntent.ShutterClick) },
    ) { viewFinderModifier ->
        CameraSession(
            modifier = viewFinderModifier,
            lensFacing = state.lensFacing,
            onCameraBound = { boundCamera ->
                camera = boundCamera
            },
            onFlashAvailabilityChanged = { isAvailable ->
                onIntent(CameraUiIntent.FlashAvailabilityChanged(isAvailable))
            },
        )
    }
}

@Composable
fun CameraContentLayout(
    modifier: Modifier = Modifier,
    remainingCount: Int,
    totalCount: Int,
    onBackClick: () -> Unit,
    onFlashClick: () -> Unit,
    onSwitchCameraClick: () -> Unit,
    onShutterClick: () -> Unit,
    viewFinder: @Composable (Modifier) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        CameraTopBar(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .padding(horizontal = 24.dp, vertical = 14.dp),
            onBackClick = onBackClick,
            remainingCount = remainingCount,
            totalCount = totalCount,
            onFlashClick = onFlashClick,
        )

        viewFinder(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        )

        CameraBottomBar(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = 32.dp, vertical = 16.dp),
            onSwitchCameraClick = onSwitchCameraClick,
            onShutterClick = onShutterClick,
        )
    }
}

@ComposePreview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CameraContentLayoutPreview() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black),
    ) {
        CameraContentLayout(
            modifier = Modifier.fillMaxSize(),
            remainingCount = 12,
            totalCount = ROOM_REQUIRED_PHOTO_COUNT,
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
