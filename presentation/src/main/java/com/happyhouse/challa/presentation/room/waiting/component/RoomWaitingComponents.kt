package com.happyhouse.challa.presentation.room.waiting.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce

@Composable
internal fun RoomWaitingTopBar(
    title: String,
    onBackClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(64.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "<",
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .size(48.dp)
                        .noRippleClickOnce(onClick = onBackClick)
                        .wrapContentSize(Alignment.Center),
                color = Color.Black,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = title,
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Color(0xFFE8E8E8),
        )
    }
}

@Composable
internal fun RoomWaitingBottomBar(
    isReadyToOpen: Boolean,
    onShareClick: () -> Unit,
    onOpenClick: () -> Unit,
) {
    Button(
        onClick = if (isReadyToOpen) onOpenClick else onShareClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(all = 20.dp)
                .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White,
            ),
    ) {
        Text(
            text = if (isReadyToOpen) "열어보기" else "일행에게 알리기",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomWaitingTopBarPreview() {
    RoomWaitingTopBar(
        title = "대기중",
        onBackClick = {},
    )
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomWaitingBottomBarPreview() {
    RoomWaitingBottomBar(
        isReadyToOpen = false,
        onShareClick = {},
        onOpenClick = {},
    )
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomWaitingBottomBarReadyPreview() {
    RoomWaitingBottomBar(
        isReadyToOpen = true,
        onShareClick = {},
        onOpenClick = {},
    )
}
