package com.happyhouse.challa.presentation.camera.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.camera.model.RemainingCaptureStatus
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
internal fun CameraRemainingCount(
    remainingCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.camera_remaining_count, remainingCount),
            color = RemainingCaptureStatus.from(remainingCount).toContentColor(),
            style = ChallaTheme.typography.bodyXSmall.medium,
        )
        Text(
            text = stringResource(R.string.camera_total_count, totalCount),
            color = ChallaTheme.colors.labelAlternative,
            style = ChallaTheme.typography.bodyXSmall.medium,
        )
    }
}

@ComposePreview(name = "6장 남음")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CameraRemainingCountPreview() {
    CameraRemainingCountPreviewContent(remainingCount = 6)
}

@ComposePreview(name = "5장 남음")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CameraRemainingCountLowRemainingPreview() {
    CameraRemainingCountPreviewContent(remainingCount = 5)
}

@ComposePreview(name = "0장 남음")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CameraRemainingCountEmptyPreview() {
    CameraRemainingCountPreviewContent(remainingCount = 0)
}

@Composable
private fun CameraRemainingCountPreviewContent(remainingCount: Int) {
    CameraRemainingCount(
        remainingCount = remainingCount,
        totalCount = 24,
    )
}

@Composable
fun RemainingCaptureStatus.toContentColor(): Color =
    when (this) {
        RemainingCaptureStatus.UNAVAILABLE -> ChallaTheme.colors.labelDisable
        RemainingCaptureStatus.LOW -> ChallaTheme.colors.statusDestructive
        RemainingCaptureStatus.AVAILABLE -> ChallaTheme.colors.primary
    }
