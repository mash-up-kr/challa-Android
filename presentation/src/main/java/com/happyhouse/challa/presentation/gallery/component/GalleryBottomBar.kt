package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.foundation.icon.ChallaIconSize
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import com.happyhouse.challa.presentation.gallery.PREVIEW_REMAINING_SECONDS
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private const val SECONDS_PER_HOUR = 3600
private const val SECONDS_PER_MINUTE = 60

/** 촬영 중 하단 바. 카메라로 이어준다. */
@Composable
fun GalleryShootBar(
    onShootClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GalleryBottomBarLayout(modifier = modifier) {
        GalleryShootButton(onClick = onShootClick)
    }
}

/** 인화 대기 하단 바. 인화 완료까지 남은 시간을 보여준다. */
@Composable
fun GalleryCountdownBar(
    remainingSeconds: Long,
    onCountdownClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GalleryBottomBarLayout(modifier = modifier) {
        GalleryCountdownButton(
            remainingSeconds = remainingSeconds,
            onClick = onCountdownClick,
        )
    }
}

/** 두 하단 바가 같은 여백과 시스템 바 처리를 쓰도록 묶는다. */
@Composable
private fun GalleryBottomBarLayout(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                .navigationBarsPadding(),
        content = content,
    )
}

/**
 * 두 하단 바 버튼이 공유하는 모양·크기·클릭 처리
 *
 * 높이는 [heightIn] min으로 잡으므로 안쪽 세로 여백을 따로 두지 않는다.
 */
private fun Modifier.bottomBarButton(
    backgroundColor: Color,
    onClick: () -> Unit,
    onClickLabel: String? = null,
): Modifier =
    this
        .fillMaxWidth()
        .heightIn(min = 54.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(backgroundColor)
        .noRippleClickOnce(
            role = Role.Button,
            onClickLabel = onClickLabel,
            onClick = onClick,
        )

/**
 * 카메라로 이어주는 CTA
 *
 * 노란 배경에 아이콘과 텍스트를 함께 두는 디자인이라,
 * ChallaTextButton(텍스트만) / ChallaIconButton(아이콘만) 어느 쪽과도 맞지 않아 따로 그린다.
 */
@Composable
private fun GalleryShootButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier.bottomBarButton(
                backgroundColor = ChallaTheme.colors.primary,
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(ChallaIconSize.V24.dp),
            painter = painterResource(ChallaIcons.Camera),
            contentDescription = null,
            tint = ChallaTheme.colors.staticBlack,
        )
        Text(
            text = stringResource(R.string.gallery_shoot),
            color = ChallaTheme.colors.staticBlack,
            style = ChallaTheme.typography.bodyLarge.bold,
        )
    }
}

/**
 * 남은 시간을 보여주는 버튼
 *
 * 이동할 화면이 없어 비활성처럼 보이지만, 눌렀을 때 아무 반응이 없지 않도록 안내로 이어준다.
 * [com.happyhouse.challa.presentation.designsystem.component.button.ChallaTextButton]의 disabled는
 * 글자색이 Label/Disable이라 디자인(Label/Alternative)과 달라 이 화면에서만 따로 그린다.
 */
@Composable
private fun GalleryCountdownButton(
    remainingSeconds: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier.bottomBarButton(
                backgroundColor = ChallaTheme.colors.backgroundLevel2,
                onClick = onClick,
                onClickLabel = stringResource(R.string.gallery_print_countdown_click_label),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text =
                stringResource(
                    R.string.gallery_print_countdown,
                    remainingSeconds.toCountdownText(),
                ),
            color = ChallaTheme.colors.labelAlternative,
            style = ChallaTheme.typography.bodyLarge.bold,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * 남은 시간을 `2:59:58` 형태로 바꾼다.
 */
private fun Long.toCountdownText(): String {
    val safeSeconds = coerceAtLeast(0L)
    val hours = safeSeconds / SECONDS_PER_HOUR
    val minutes = (safeSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val seconds = safeSeconds % SECONDS_PER_MINUTE

    return "$hours:${minutes.toTwoDigits()}:${seconds.toTwoDigits()}"
}

private fun Long.toTwoDigits(): String = toString().padStart(2, '0')

@ComposePreview(
    showBackground = true,
    widthDp = 390,
    name = "BottomBar - 촬영 중",
)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryShootBarPreview() {
    GalleryShootBar(onShootClick = {})
}

@ComposePreview(
    showBackground = true,
    widthDp = 390,
    name = "BottomBar - 인화 대기",
)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryCountdownBarPreview() {
    GalleryCountdownBar(
        remainingSeconds = PREVIEW_REMAINING_SECONDS,
        onCountdownClick = {},
    )
}
