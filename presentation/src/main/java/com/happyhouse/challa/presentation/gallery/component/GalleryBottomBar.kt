package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.CHALLA_PREVIEW_BACKGROUND
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import com.happyhouse.challa.presentation.gallery.PREVIEW_REMAINING_SECONDS
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private const val SECONDS_PER_HOUR = 3600
private const val SECONDS_PER_MINUTE = 60

private val CountdownButtonShape = RoundedCornerShape(12.dp)
private val CountdownButtonMinHeight = 54.dp
private val BottomBarTopPadding = 8.dp

/**
 * 인화 전 하단 바
 */
@Composable
fun GalleryBottomBar(
    remainingSeconds: Long,
    onCountdownClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = BottomBarTopPadding)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
    ) {
        GalleryCountdownButton(
            remainingSeconds = remainingSeconds,
            onClick = onCountdownClick,
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
            modifier
                .fillMaxWidth()
                .heightIn(min = CountdownButtonMinHeight)
                .clip(CountdownButtonShape)
                .background(ChallaTheme.colors.backgroundLevel2)
                .noRippleClickOnce(
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.gallery_print_countdown_click_label),
                    onClick = onClick,
                ).padding(horizontal = 20.dp, vertical = 14.dp),
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

@ComposePreview(showBackground = true, backgroundColor = CHALLA_PREVIEW_BACKGROUND, widthDp = 390)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryBottomBarPreview() {
    GalleryBottomBar(
        remainingSeconds = PREVIEW_REMAINING_SECONDS,
        onCountdownClick = {},
    )
}
