package com.happyhouse.challa.presentation.room.main.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.Black
import com.happyhouse.challa.presentation.designsystem.theme.Border
import com.happyhouse.challa.presentation.designsystem.theme.Gray700
import com.happyhouse.challa.presentation.model.Room
import com.happyhouse.challa.presentation.model.RoomStatus
import com.happyhouse.challa.presentation.room.main.util.roomMainDescription
import com.happyhouse.challa.presentation.room.main.util.roomMainLabel
import kotlin.time.Duration.Companion.hours

@Composable
internal fun StatusCard(room: Room) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Border,
                    shape = RoundedCornerShape(6.dp),
                )
                .padding(horizontal = 18.dp, vertical = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "상태",
                modifier = Modifier.weight(1f),
                color = Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Box(
                modifier =
                    Modifier
                        .border(
                            width = 1.dp,
                            color = Black,
                            shape = RoundedCornerShape(1000.dp),
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = room.status.roomMainLabel,
                    color = Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = room.status.roomMainDescription(room.requiredPhotoCount),
            color = Gray700,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp,
        )
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun StatusCardShootingPreview() {
    StatusCard(
        room =
            Room(
                id = "room-id",
                name = "해피하우스 프작모",
                status = RoomStatus.Shooting(taken = 11),
            ),
    )
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun StatusCardWaitingPreview() {
    StatusCard(
        room =
            Room(
                id = "room-id",
                name = "해피하우스 프작모",
                status = RoomStatus.Waiting(dDay = 0, remaining = 3.hours),
            ),
    )
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun StatusCardPublishedPreview() {
    StatusCard(
        room =
            Room(
                id = "room-id",
                name = "해피하우스 프작모",
                status = RoomStatus.Opened,
            ),
    )
}
