package com.happyhouse.challa.presentation.camera.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.happyhouse.challa.presentation.camera.component.CAMERA_BEZEL_ASPECT_RATIO
import com.happyhouse.challa.presentation.camera.component.CameraBezelBorderWidth
import com.happyhouse.challa.presentation.camera.component.CameraBezelCornerRadius
import com.happyhouse.challa.presentation.camera.component.CameraBezelHorizontalPadding
import com.happyhouse.challa.presentation.camera.component.CameraBezelTopPadding
import com.happyhouse.challa.presentation.camera.component.CameraContentLayout
import com.happyhouse.challa.presentation.camera.component.CameraControlsTopSpacing
import com.happyhouse.challa.presentation.camera.component.CameraShutterButtonInnerSize
import com.happyhouse.challa.presentation.camera.component.CameraShutterButtonSize
import com.happyhouse.challa.presentation.camera.model.CameraFilterUiModel
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.model.ROOM_REQUIRED_PHOTO_COUNT
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun CameraOnboardingOverlay(modifier: Modifier = Modifier) {
    val dimColor = ChallaTheme.colors.materialDimmer
    val bezelColor = ChallaTheme.colors.staticWhite
    val shutterColor = ChallaTheme.colors.primary
    val shutterInnerColor = ChallaTheme.colors.staticWhite

    Canvas(
        modifier =
            modifier
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent().changes.forEach { it.consume() }
                        }
                    }
                },
    ) {
        drawRect(color = dimColor)

        val bezelBorderWidth = CameraBezelBorderWidth.toPx()
        val bezelLeft = CameraBezelHorizontalPadding.toPx()
        val bezelTop = CameraBezelTopPadding.toPx()
        val bezelWidth = (size.width - bezelLeft * 2).coerceAtLeast(0f)
        val bezelHeight = bezelWidth / CAMERA_BEZEL_ASPECT_RATIO
        val bezelStrokeInset = bezelBorderWidth / 2

        drawRoundRect(
            color = bezelColor,
            topLeft = Offset(bezelLeft + bezelStrokeInset, bezelTop + bezelStrokeInset),
            size = Size(bezelWidth - bezelBorderWidth, bezelHeight - bezelBorderWidth),
            cornerRadius = CornerRadius(CameraBezelCornerRadius.toPx()),
            style = Stroke(width = bezelBorderWidth),
        )

        val shutterRadius = CameraShutterButtonSize.toPx() / 2
        val shutterCenter =
            Offset(
                x = size.width / 2,
                y = bezelTop + bezelHeight + CameraControlsTopSpacing.toPx() + shutterRadius,
            )
        drawCircle(
            color = shutterColor,
            radius = shutterRadius - bezelStrokeInset,
            center = shutterCenter,
            style = Stroke(width = bezelBorderWidth),
        )
        drawCircle(
            color = shutterInnerColor,
            radius = CameraShutterButtonInnerSize.toPx() / 2,
            center = shutterCenter,
        )
    }
}

@Preview
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun CameraOnboardingPreview() {
    CameraContentLayout(
        modifier = Modifier.fillMaxSize(),
        roomName = "해피하우스강릉여행",
        remainingCount = 6,
        totalCount = ROOM_REQUIRED_PHOTO_COUNT,
        filters = persistentListOf(CameraFilterUiModel.Original),
        selectedFilterIndex = 0,
        isFlashEnabled = false,
        isCameraSwitchEnabled = true,
        shutterEnabled = true,
        isShutterEffectVisible = false,
        isOnboardingVisible = true,
        zoomLevel = 1f,
        onFlashClick = {},
        onSwitchCameraClick = {},
        onShutterClick = {},
        onZoomClick = {},
        onFilterClick = {},
        onRoomInfoClick = {},
        viewFinder = {},
    )
}
