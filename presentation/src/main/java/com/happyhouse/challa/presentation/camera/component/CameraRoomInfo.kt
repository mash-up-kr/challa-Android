package com.happyhouse.challa.presentation.camera.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.foundation.icon.ChallaIconSize
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
fun CameraRoomInfo(
    roomName: String,
    remainingCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(1000.dp))
                    .background(ChallaTheme.colors.backgroundLevel3)
                    .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.widthIn(max = 240.dp),
                text = roomName,
                color = ChallaTheme.colors.labelNormal,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = ChallaTheme.typography.bodyXSmall.medium,
            )
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                painter = painterResource(R.drawable.ic_unfold_more),
                contentDescription = stringResource(R.string.camera_room_selector_description),
                modifier = Modifier.size(ChallaIconSize.SMALL.dp),
                tint = ChallaTheme.colors.labelNeutral,
            )
        }

        Row(modifier = Modifier.padding(top = 12.dp)) {
            Text(
                text = stringResource(R.string.camera_remaining_count, remainingCount),
                color =
                    when {
                        remainingCount <= 0 -> ChallaTheme.colors.labelDisable
                        remainingCount <= 3 -> ChallaTheme.colors.primaryOrange
                        else -> ChallaTheme.colors.primaryYellow
                    },
                style = ChallaTheme.typography.bodyXSmall.medium,
            )
            Text(
                text = stringResource(R.string.camera_total_count, totalCount),
                color = ChallaTheme.colors.labelAlternative,
                style = ChallaTheme.typography.bodyXSmall.medium,
            )
        }
    }
}

@ComposePreview(name = "6장 남음", showBackground = true)
@Composable
private fun CameraRoomInfoPreview() {
    CameraRoomInfoPreviewContent(remainingCount = 6)
}

@ComposePreview(name = "3장 남음", showBackground = true)
@Composable
private fun CameraRoomInfoThreeRemainingPreview() {
    CameraRoomInfoPreviewContent(remainingCount = 3)
}

@ComposePreview(name = "0장 남음", showBackground = true)
@Composable
private fun CameraRoomInfoEmptyPreview() {
    CameraRoomInfoPreviewContent(remainingCount = 0)
}

@Composable
private fun CameraRoomInfoPreviewContent(remainingCount: Int) {
    ChallaTheme {
        Box(
            modifier =
                Modifier
                    .background(Color.Black)
                    .padding(12.dp),
        ) {
            CameraRoomInfo(
                roomName = "해피하우스강릉여행",
                remainingCount = remainingCount,
                totalCount = 24,
            )
        }
    }
}
