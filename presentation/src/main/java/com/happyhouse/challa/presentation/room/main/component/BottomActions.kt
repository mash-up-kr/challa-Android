package com.happyhouse.challa.presentation.room.main.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.Black
import com.happyhouse.challa.presentation.designsystem.theme.White
import com.happyhouse.challa.presentation.designsystem.util.rememberClickOnce
import com.happyhouse.challa.presentation.model.RoomStatus
import com.happyhouse.challa.presentation.room.main.util.isRoomMainPrimaryButtonEnabled
import com.happyhouse.challa.presentation.room.main.util.roomMainPrimaryButtonText
import kotlin.time.Duration.Companion.hours

@Composable
internal fun BottomActions(
    status: RoomStatus,
    modifier: Modifier = Modifier,
    onShareClick: () -> Unit = {},
    onShootClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ShareButton(onClick = onShareClick)
        RoomMainPrimaryButton(
            status = status,
            onShootClick = onShootClick,
            onGalleryClick = onGalleryClick,
        )
    }
}

@Composable
private fun ShareButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = rememberClickOnce(onClick = onClick),
        modifier =
            modifier
                .fillMaxWidth()
                .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(width = 1.dp, color = Color(0xFF9E9E9E)),
        colors =
            ButtonDefaults.outlinedButtonColors(
                contentColor = Black,
            ),
    ) {
        Text(
            text = "초대 링크 공유",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun RoomMainPrimaryButton(
    status: RoomStatus,
    onShootClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick =
            rememberClickOnce(
                onClick = {
                    when (status) {
                        is RoomStatus.Shooting -> onShootClick()
                        is RoomStatus.Waiting -> Unit
                        RoomStatus.Opened,
                        is RoomStatus.Expiring,
                        -> onGalleryClick()
                    }
                },
            ),
        enabled = status.isRoomMainPrimaryButtonEnabled,
        modifier =
            modifier
                .fillMaxWidth()
                .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Black,
                contentColor = White,
            ),
    ) {
        Text(
            text = status.roomMainPrimaryButtonText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun BottomActionsShootingPreview() {
    BottomActions(status = RoomStatus.Shooting(taken = 11))
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun BottomActionsWaitingPreview() {
    BottomActions(status = RoomStatus.Waiting(dDay = 0, remaining = 3.hours))
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun BottomActionsPublishedPreview() {
    BottomActions(status = RoomStatus.Opened)
}
