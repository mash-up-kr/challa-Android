package com.happyhouse.challa.presentation.camera.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.camera.component.CameraContentLayout
import com.happyhouse.challa.presentation.camera.model.CameraFilterUiModel
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbar
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarContent
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.model.ROOM_REQUIRED_PHOTO_COUNT
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun CameraOnboardingOverlay(modifier: Modifier = Modifier) {
    val backdropBrush =
        Brush.verticalGradient(
            colors =
                listOf(
                    Color.Black.copy(alpha = 0.3f),
                    Color.Black,
                ),
        )

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
        drawRect(brush = backdropBrush)
    }
}

@Preview
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun CameraOnboardingPreview() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(ChallaTheme.colors.staticBlack.copy(alpha = 0.9f)),
    ) {
        CameraContentLayout(
            modifier = Modifier.fillMaxSize(),
            roomName = "해피하우스강릉여행",
            remainingCount = 6,
            totalCount = ROOM_REQUIRED_PHOTO_COUNT,
            isRoomLoaded = true,
            isFilterSelectorReady = true,
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

        CameraOnboardingOverlay(modifier = Modifier.fillMaxSize())

        ChallaSnackbar(
            content =
                ChallaSnackbarContent.HeadingOnly(
                    heading = "셔터를 누르는 순간 장수가 차감돼요.",
                ),
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            actionLabel = "다음",
            actionLabelColor = ChallaTheme.colors.primary,
            onActionClick = {},
        )
    }
}
