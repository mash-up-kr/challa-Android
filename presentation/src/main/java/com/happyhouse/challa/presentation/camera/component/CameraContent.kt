package com.happyhouse.challa.presentation.camera.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.camera.contract.CameraIntent
import com.happyhouse.challa.presentation.camera.contract.CameraState
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.model.ROOM_REQUIRED_PHOTO_COUNT
import androidx.camera.core.Camera as CameraXCamera
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private const val CAMERA_BEZEL_ASPECT_RATIO = 313f / 401f

@Composable
fun CameraContent(
    modifier: Modifier = Modifier,
    state: CameraState,
    onIntent: (CameraIntent) -> Unit,
) {
    var camera by remember { mutableStateOf<CameraXCamera?>(null) }

    LaunchedEffect(camera, state.isFlashOn, state.hasFlashUnit) {
        camera?.cameraControl?.enableTorch(state.isFlashOn && state.hasFlashUnit)
    }

    CameraContentLayout(
        modifier = modifier,
        roomName = state.roomName,
        remainingCount = state.remainingCount,
        totalCount = state.totalCount,
        selectedFilterIndex = state.selectedFilterIndex,
        isFlashOn = state.isFlashOn,
        onFlashClick = { onIntent(CameraIntent.FlashClick) },
        onSwitchCameraClick = { onIntent(CameraIntent.SwitchCameraClick) },
        onShutterClick = { onIntent(CameraIntent.ShutterClick) },
        onFilterClick = { onIntent(CameraIntent.FilterClick(it)) },
    ) { viewFinderModifier ->
        CameraSession(
            modifier = viewFinderModifier,
            lensFacing = state.lensFacing,
            onCameraBound = { camera = it },
            onFlashAvailabilityChanged = { onIntent(CameraIntent.FlashAvailabilityChanged(it)) },
        )
    }
}

@Composable
fun CameraContentLayout(
    roomName: String,
    remainingCount: Int,
    totalCount: Int,
    selectedFilterIndex: Int,
    isFlashOn: Boolean,
    onFlashClick: () -> Unit,
    onSwitchCameraClick: () -> Unit,
    onShutterClick: () -> Unit,
    onFilterClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewFinder: @Composable (Modifier) -> Unit,
) {
    Column(
        modifier = modifier.background(ChallaTheme.colors.staticBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CameraBezel(
            isPhotoLimitReached = remainingCount <= 0,
            modifier =
                Modifier
                    .padding(start = 36.dp, top = 40.dp, end = 36.dp)
                    .fillMaxWidth()
                    .aspectRatio(CAMERA_BEZEL_ASPECT_RATIO),
            viewFinder = viewFinder,
        )

        Spacer(modifier = Modifier.height(20.dp))

        CameraControls(
            modifier = Modifier,
            isFlashOn = isFlashOn,
            shutterEnabled = remainingCount > 0,
            onFlashClick = onFlashClick,
            onSwitchCameraClick = onSwitchCameraClick,
            onShutterClick = onShutterClick,
        )

        Spacer(modifier = Modifier.height(20.dp))

        CameraFilterSelector(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            selectedFilterIndex = selectedFilterIndex,
            onFilterClick = onFilterClick,
        )

        Spacer(modifier = Modifier.weight(1f))

        CameraRoomInfo(
            roomName = roomName,
            remainingCount = remainingCount,
            totalCount = totalCount,
            modifier = Modifier.padding(bottom = 40.dp),
        )
    }
}

@ComposePreview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CameraContentLayoutPreview() {
    CameraContentLayout(
        modifier = Modifier.fillMaxSize(),
        roomName = "해피하우스강릉여행",
        remainingCount = 6,
        totalCount = ROOM_REQUIRED_PHOTO_COUNT,
        selectedFilterIndex = 2,
        isFlashOn = false,
        onFlashClick = {},
        onSwitchCameraClick = {},
        onShutterClick = {},
        onFilterClick = {},
        viewFinder = { MockViewFinder(it) },
    )
}

@ComposePreview(showBackground = true, widthDp = 342, heightDp = 754)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CameraContentLimitReachedPreview() {
    CameraContentLayout(
        modifier = Modifier.fillMaxSize(),
        roomName = "방이름방이름방이름3",
        remainingCount = 0,
        totalCount = 48,
        selectedFilterIndex = 2,
        isFlashOn = false,
        onFlashClick = {},
        onSwitchCameraClick = {},
        onShutterClick = {},
        onFilterClick = {},
        viewFinder = { MockViewFinder(it) },
    )
}
