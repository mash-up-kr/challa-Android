package com.happyhouse.challa.presentation.room.main.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.rememberClickOnce
import com.happyhouse.challa.presentation.model.RoomStatus
import com.happyhouse.challa.presentation.room.main.util.isRoomMainActionButtonEnabled
import com.happyhouse.challa.presentation.room.main.util.roomMainActionButtonText
import kotlin.time.Duration.Companion.hours

@Composable
internal fun RoomMainActionButton(
    status: RoomStatus,
    modifier: Modifier = Modifier,
    onShootClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
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
        enabled = status.isRoomMainActionButtonEnabled,
        modifier =
            modifier
                .fillMaxWidth()
                .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = ChallaTheme.colors.staticBlack,
                contentColor = ChallaTheme.colors.staticWhite,
            ),
    ) {
        Text(
            text = status.roomMainActionButtonText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomMainActionButtonShootingPreview() {
    RoomMainActionButton(status = RoomStatus.Shooting(taken = 11))
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomMainActionButtonWaitingPreview() {
    RoomMainActionButton(status = RoomStatus.Waiting(dDay = 0, remaining = 3.hours))
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomMainActionButtonPublishedPreview() {
    RoomMainActionButton(status = RoomStatus.Opened)
}
