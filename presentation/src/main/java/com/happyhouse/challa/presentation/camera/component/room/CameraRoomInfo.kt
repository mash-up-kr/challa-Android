package com.happyhouse.challa.presentation.camera.component.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.camera.model.RemainingCaptureStatus
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
internal fun CameraRoomInfo(
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
@Composable
private fun CameraRoomInfoPreview() {
    CameraRoomInfoPreviewContent(remainingCount = 6)
}

@ComposePreview(name = "5장 남음")
@Composable
private fun CameraRoomInfoLowRemainingPreview() {
    CameraRoomInfoPreviewContent(remainingCount = 5)
}

@ComposePreview(name = "0장 남음")
@Composable
private fun CameraRoomInfoEmptyPreview() {
    CameraRoomInfoPreviewContent(remainingCount = 0)
}

@Composable
private fun CameraRoomInfoPreviewContent(remainingCount: Int) {
    ChallaTheme {
        CameraRoomInfo(
            remainingCount = remainingCount,
            totalCount = 24,
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }
}

@Composable
fun RemainingCaptureStatus.toContentColor(): Color =
    when (this) {
        RemainingCaptureStatus.UNAVAILABLE -> ChallaTheme.colors.labelDisable
        RemainingCaptureStatus.LOW -> ChallaTheme.colors.statusDestructive
        RemainingCaptureStatus.AVAILABLE -> ChallaTheme.colors.primary
    }
