package com.happyhouse.challa.presentation.camera.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.dashedRoundedBorder
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce

internal val CameraBezelCornerRadius = 60.dp
internal val CameraBezelBorderWidth = 4.dp

@Composable
internal fun CameraBezel(
    isPhotoLimitReached: Boolean,
    isShutterEffectVisible: Boolean,
    zoomLevel: Float,
    onZoomClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewFinder: @Composable (Modifier) -> Unit,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(CameraBezelCornerRadius))
                .background(ChallaTheme.colors.staticBlack)
                .border(
                    CameraBezelBorderWidth,
                    ChallaTheme.colors.staticWhite,
                    RoundedCornerShape(CameraBezelCornerRadius),
                )
                .padding(all = 24.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(30.dp))
                    .background(ChallaTheme.colors.staticBlack),
        ) {
            viewFinder(Modifier.fillMaxSize())

            if (isPhotoLimitReached) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(ChallaTheme.colors.backgroundLevel1)
                            .dashedRoundedBorder(
                                color = ChallaTheme.colors.backgroundLevel4,
                                cornerRadius = 30.dp,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.camera_photo_limit_reached),
                        color = ChallaTheme.colors.labelDisable,
                        style = ChallaTheme.typography.bodyLarge.medium,
                    )
                }
            } else {
                Text(
                    text =
                        stringResource(
                            R.string.camera_zoom_level,
                            zoomLevel.toDisplayText(),
                        ),
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(650.dp))
                            .background(ChallaTheme.colors.backgroundLevel4)
                            .noRippleClickOnce(
                                role = Role.Button,
                                onClick = onZoomClick,
                            )
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    color = ChallaTheme.colors.labelNormal,
                    style = ChallaTheme.typography.bodyMedium.bold,
                )
            }

            if (isShutterEffectVisible) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(ChallaTheme.colors.staticBlack),
                )
            }
        }
    }
}

@Preview(
    name = "촬영 가능",
    showBackground = true,
    widthDp = 313,
    heightDp = 401,
)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CameraBezelPreview() {
    ChallaTheme {
        CameraBezel(
            modifier = Modifier.fillMaxSize(),
            isPhotoLimitReached = false,
            isShutterEffectVisible = false,
            zoomLevel = 1f,
            onZoomClick = {},
            viewFinder = {},
        )
    }
}

@Preview(
    name = "장수 소진",
    showBackground = true,
    widthDp = 313,
    heightDp = 401,
)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CameraBezelLimitReachedPreview() {
    ChallaTheme {
        CameraBezel(
            modifier = Modifier.fillMaxSize(),
            isPhotoLimitReached = true,
            isShutterEffectVisible = false,
            zoomLevel = 2f,
            onZoomClick = {},
            viewFinder = {},
        )
    }
}

private fun Float.toDisplayText(): String =
    if (this % 1f == 0f) {
        toInt().toString()
    } else {
        toString()
    }
