package com.happyhouse.challa.presentation.home.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

private const val SECONDS_PER_HOUR = 3600
private const val SECONDS_PER_MINUTE = 60

private val BadgeIconSize = 22.dp
private val BadgeShape = RoundedCornerShape(12.dp)

/**
 * 인화 전 사진(촬영 중·인화 대기 카드) 위에 얹히는 상태 배지.
 *
 * 모양(패딩·간격·라운드)은 공유하고, 아이콘·문구·색만 바꿔 두 종류로 쓴다.
 * - [HomeCameraBadge] : 촬영한 사진 수 (노란 배경)
 * - [HomeTimerBadge] : 인화 완료까지 남은 시간 (어두운 배경)
 */
@Composable
private fun HomeCardBadge(
    @DrawableRes iconRes: Int,
    iconTint: Color,
    text: String,
    textColor: Color,
    backgroundColor: Color,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clip(BadgeShape)
                .background(backgroundColor)
                .padding(horizontal = 11.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(BadgeIconSize),
            tint = iconTint,
        )
        Text(
            text = text,
            color = textColor,
            style = ChallaTheme.typography.bodyMedium.bold,
        )
    }
}

/** 촬영 촬영버튼 배지. `촬영수/전체수` 형태로 표기한다. */
@Composable
fun HomeCameraBadge(
    takenCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    HomeCardBadge(
        iconRes = ChallaIcons.Camera,
        iconTint = ChallaTheme.colors.staticBlack,
        text = stringResource(id = R.string.home_taken_count, takenCount, totalCount),
        textColor = ChallaTheme.colors.staticBlack,
        backgroundColor = ChallaTheme.colors.primary,
        contentDescription = stringResource(id = R.string.home_taken_count_description),
        modifier = modifier,
    )
}

/** 남은시간버튼 배지. 인화 완료까지 남은 시간을 `2:15:32` 형태로 표기한다. */
@Composable
fun HomeTimerBadge(
    remainingSeconds: Long,
    modifier: Modifier = Modifier,
) {
    HomeCardBadge(
        iconRes = ChallaIcons.Clock,
        iconTint = ChallaTheme.colors.labelNeutral,
        text = remainingSeconds.toCountdownText(),
        textColor = ChallaTheme.colors.labelNeutral,
        backgroundColor = ChallaTheme.colors.backgroundLevel4,
        contentDescription = stringResource(id = R.string.home_remaining_time_description),
        modifier = modifier,
    )
}

/** 남은 시간을 `2:15:32` 형태로 바꾼다. 음수는 0으로 막는다. */
private fun Long.toCountdownText(): String {
    val safeSeconds = coerceAtLeast(0L)
    val hours = safeSeconds / SECONDS_PER_HOUR
    val minutes = (safeSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val seconds = safeSeconds % SECONDS_PER_MINUTE
    return "$hours:${minutes.toTwoDigits()}:${seconds.toTwoDigits()}"
}

private fun Long.toTwoDigits(): String = toString().padStart(2, '0')

@Preview(showBackground = true, backgroundColor = 0xFF111111)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun HomeCameraBadgePreview() {
    HomeCameraBadge(takenCount = 24, totalCount = 24)
}

@Preview(showBackground = true, backgroundColor = 0xFF111111)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun HomeTimerBadgePreview() {
    // 2:15:32
    HomeTimerBadge(remainingSeconds = 8132L)
}
