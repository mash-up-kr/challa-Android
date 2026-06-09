package com.happyhouse.challa.presentation.room.waiting

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.room.waiting.component.RoomWaitingBottomBar
import com.happyhouse.challa.presentation.room.waiting.component.RoomWaitingTopBar
import com.happyhouse.challa.presentation.room.waiting.contract.RoomWaitingUiState
import kotlinx.collections.immutable.ImmutableList

@Composable
fun RoomWaitingScreen(
    uiState: RoomWaitingUiState = RoomWaitingUiState(),
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onOpenClick: () -> Unit = {},
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White),
    ) {
        RoomWaitingTopBar(
            title = "대기중",
            onBackClick = onBackClick,
        )

        RoomWaitingContent(
            uiState = uiState,
            modifier = Modifier.weight(1f),
        )

        RoomWaitingBottomBar(
            isReadyToOpen = uiState.isReadyToOpen,
            onShareClick = onShareClick,
            onOpenClick = onOpenClick,
        )
    }
}

@Composable
private fun RoomWaitingContent(
    uiState: RoomWaitingUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        FilmCanisterPlaceholder()

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = uiState.remainingSeconds.toTimerText().replace(":", " : "),
            color = Color.Black,
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "시간이 흐른 뒤 다 같이 열어봐요",
            color = Color(0xFF6B6B6B),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(20.dp))

        MemberAvatarRow(memberInitials = uiState.memberInitials)

        Spacer(modifier = Modifier.height(32.dp))
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun FilmCanisterPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .size(220.dp)
                .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val stripeColor = Color(0xFFF7F7F7)
            val borderColor = Color(0xFFE6E6E6)
            val stripeWidth = 18.dp.toPx()
            var startX = -size.height

            while (startX < size.width) {
                drawLine(
                    color = stripeColor,
                    start = Offset(startX, size.height),
                    end = Offset(startX + size.height, 0f),
                    strokeWidth = stripeWidth,
                )
                startX += stripeWidth * 2f
            }

            drawCircle(
                color = borderColor,
                style = Stroke(width = 2.dp.toPx()),
            )
        }
        Text(
            text = "[필름통 일러스트]",
            color = Color(0xFF8D8D8D),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun MemberAvatarRow(memberInitials: ImmutableList<String>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        memberInitials.forEach { memberInitial ->
            MemberAvatar(initial = memberInitial)
        }
    }
}

@Composable
private fun MemberAvatar(initial: String) {
    val avatarColor =
        when (initial) {
            "박" -> Color(0xFFFFD8A8)
            "김" -> Color(0xFFAED8F6)
            "이" -> Color(0xFFC8EBC8)
            else -> Color(0xFFE6E6E6)
        }

    Box(
        modifier =
            Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(avatarColor)
                .border(
                    width = 1.5.dp,
                    color = Color(0xFFCFCFCF),
                    shape = CircleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial.take(1),
            color = Color(0xFF595959),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomWaitingScreenPreview() {
    RoomWaitingScreen(
        uiState =
            RoomWaitingUiState(
                remainingSeconds = 10_035L,
            ),
    )
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomWaitingReadyPreview() {
    RoomWaitingScreen()
}

private fun Long.toTimerText(): String {
    val totalSeconds = coerceAtLeast(0L)
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L

    return "${hours.twoDigits()}:${minutes.twoDigits()}:${seconds.twoDigits()}"
}

private fun Long.twoDigits(): String = toString().padStart(2, '0')
