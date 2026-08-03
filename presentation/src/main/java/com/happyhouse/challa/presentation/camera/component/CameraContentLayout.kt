package com.happyhouse.challa.presentation.camera.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.camera.camerax.PreviewViewfinderPlaceholder
import com.happyhouse.challa.presentation.camera.component.room.CameraRoomInfo
import com.happyhouse.challa.presentation.camera.model.CameraFilterUiModel
import com.happyhouse.challa.presentation.camera.model.RemainingCaptureStatus
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.model.ROOM_REQUIRED_PHOTO_COUNT
import kotlinx.collections.immutable.ImmutableList

private const val CAMERA_BEZEL_ASPECT_RATIO = 313f / 401f

@Composable
internal fun CameraContentLayout(
    roomName: String,
    remainingCount: Int,
    totalCount: Int,
    filters: ImmutableList<CameraFilterUiModel>,
    selectedFilterIndex: Int,
    isFlashEnabled: Boolean,
    isCameraSwitchEnabled: Boolean,
    shutterEnabled: Boolean,
    isShutterEffectVisible: Boolean,
    zoomLevel: Float,
    onFlashClick: () -> Unit,
    onSwitchCameraClick: () -> Unit,
    onShutterClick: () -> Unit,
    onZoomClick: () -> Unit,
    onFilterClick: (Int) -> Unit,
    onRoomInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewFinder: @Composable (Modifier) -> Unit,
) {
    val remainingCaptureStatus = RemainingCaptureStatus.from(remainingCount)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CameraBezel(
            isPhotoLimitReached = remainingCaptureStatus == RemainingCaptureStatus.UNAVAILABLE,
            isShutterEffectVisible = isShutterEffectVisible,
            zoomLevel = zoomLevel,
            onZoomClick = onZoomClick,
            modifier =
                Modifier
                    .padding(start = 36.dp, top = 40.dp, end = 36.dp)
                    .fillMaxWidth()
                    .aspectRatio(CAMERA_BEZEL_ASPECT_RATIO),
            viewFinder = viewFinder,
        )

        Spacer(modifier = Modifier.height(20.dp))

        CameraControls(
            isFlashEnabled = isFlashEnabled,
            isCameraSwitchEnabled = isCameraSwitchEnabled,
            shutterEnabled = shutterEnabled,
            onFlashClick = onFlashClick,
            onSwitchCameraClick = onSwitchCameraClick,
            onShutterClick = onShutterClick,
        )

        Spacer(modifier = Modifier.height(20.dp))

        CameraFilterSelector(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            filters = filters,
            selectedFilterIndex = selectedFilterIndex,
            onFilterClick = onFilterClick,
        )

        Spacer(modifier = Modifier.weight(1f))

        CameraRoomInfo(
            roomName = roomName,
            remainingCount = remainingCount,
            totalCount = totalCount,
            onClick = onRoomInfoClick,
            modifier = Modifier.padding(bottom = 40.dp),
        )
    }
}

@Preview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CameraContentLayoutPreview() {
    CameraContentLayout(
        modifier = Modifier.fillMaxSize(),
        roomName = "해피하우스강릉여행",
        remainingCount = 6,
        totalCount = ROOM_REQUIRED_PHOTO_COUNT,
        filters = previewCameraFilters,
        selectedFilterIndex = 0,
        isFlashEnabled = false,
        isCameraSwitchEnabled = true,
        shutterEnabled = true,
        isShutterEffectVisible = false,
        zoomLevel = 1f,
        onFlashClick = {},
        onSwitchCameraClick = {},
        onShutterClick = {},
        onZoomClick = {},
        onFilterClick = {},
        onRoomInfoClick = {},
        viewFinder = { PreviewViewfinderPlaceholder(it) },
    )
}

@Preview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CameraContentLimitReachedPreview() {
    CameraContentLayout(
        modifier = Modifier.fillMaxSize(),
        roomName = "방이름방이름방이름3",
        remainingCount = 0,
        totalCount = 48,
        filters = previewCameraFilters,
        selectedFilterIndex = 0,
        isFlashEnabled = false,
        isCameraSwitchEnabled = true,
        shutterEnabled = false,
        isShutterEffectVisible = false,
        zoomLevel = 1f,
        onFlashClick = {},
        onSwitchCameraClick = {},
        onShutterClick = {},
        onZoomClick = {},
        onFilterClick = {},
        onRoomInfoClick = {},
        viewFinder = { PreviewViewfinderPlaceholder(it) },
    )
}

private val previewCameraFilters =
    kotlinx.collections.immutable.persistentListOf(
        CameraFilterUiModel.Original,
        CameraFilterUiModel.Remote(name = "필터1", fileUrl = "https://example.com/filter1.cube"),
        CameraFilterUiModel.Remote(name = "필터2", fileUrl = "https://example.com/filter2.cube"),
    )
