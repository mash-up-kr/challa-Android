package com.happyhouse.challa.presentation.camera.component

import androidx.camera.core.CameraSelector
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import androidx.camera.core.Camera as CameraXCamera
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
fun CameraContent(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
) {
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var isFlashOn by remember { mutableStateOf(false) }
    var hasFlashUnit by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<CameraXCamera?>(null) }

    LaunchedEffect(camera, isFlashOn, hasFlashUnit) {
        camera?.cameraControl?.enableTorch(isFlashOn && hasFlashUnit)
    }

    CameraContentLayout(
        modifier = modifier,
        isFlashOn = isFlashOn,
        isFlashEnabled = hasFlashUnit,
        onBackClick = onBackClick,
        onFlashClick = {
            if (hasFlashUnit) {
                isFlashOn = !isFlashOn
            }
        },
        onSwitchCameraClick = {
            isFlashOn = false
            lensFacing =
                if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }
        },
        onShutterClick = {},
    ) { viewFinderModifier ->
        CameraSession(
            modifier = viewFinderModifier,
            lensFacing = lensFacing,
            onCameraBound = { boundCamera ->
                camera = boundCamera
            },
            onFlashAvailabilityChanged = { isAvailable ->
                hasFlashUnit = isAvailable
                if (!isAvailable) {
                    isFlashOn = false
                }
            },
        )
    }
}

@Composable
fun CameraContentLayout(
    modifier: Modifier = Modifier,
    isFlashOn: Boolean,
    isFlashEnabled: Boolean,
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
                    .padding(horizontal = 28.dp, vertical = 14.dp),
            onBackClick = onBackClick,
            remainingCount = 12,
            totalCount = 24,
            isFlashOn = isFlashOn,
            isFlashEnabled = isFlashEnabled,
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
                    .height(136.dp)
                    .padding(horizontal = 56.dp, vertical = 16.dp),
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
