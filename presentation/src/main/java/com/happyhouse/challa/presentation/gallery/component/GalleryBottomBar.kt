package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaTextButton
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.util.clickOnce
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private const val SECONDS_PER_HOUR = 3600
private const val SECONDS_PER_MINUTE = 60

/**
 * 인화 전 하단 바
 *
 * 남은 시간을 보여줄 뿐 이동할 화면이 없어 버튼 자체는 비활성 스타일을 쓴다.
 * 대신 눌렀을 때 아무 반응이 없지 않도록 클릭은 바깥에서 받아 안내로 이어준다.
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
                .padding(top = 8.dp)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
    ) {
        Box {
            ChallaTextButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.gallery_print_countdown, remainingSeconds.toCountdownText()),
                onClick = {},
                enabled = false,
            )

            // 비활성 버튼은 터치를 삼키기만 하므로, 위에 클릭 영역을 덮어 안내로 이어준다.
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .clickOnce(
                            role = Role.Button,
                            onClickLabel = stringResource(R.string.gallery_print_countdown_click_label),
                            onClick = onCountdownClick,
                        ),
            )
        }
    }
}

/**
 * 남은 초를 `2:59:58` 형태로 바꾼다.
 */
private fun Long.toCountdownText(): String {
    val safeSeconds = coerceAtLeast(0L)
    val hours = safeSeconds / SECONDS_PER_HOUR
    val minutes = (safeSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val seconds = safeSeconds % SECONDS_PER_MINUTE

    return "$hours:${minutes.toTwoDigits()}:${seconds.toTwoDigits()}"
}

private fun Long.toTwoDigits(): String = toString().padStart(2, '0')

@ComposePreview(showBackground = true, backgroundColor = 0xFF111111, widthDp = 390)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryBottomBarPreview() {
    GalleryBottomBar(
        remainingSeconds = 10_798L,
        onCountdownClick = {},
    )
}
