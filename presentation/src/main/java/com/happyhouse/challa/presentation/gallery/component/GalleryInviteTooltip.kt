package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

/** 초대 메뉴 아래에 붙는 안내 툴팁 */
@Composable
fun GalleryInviteTooltip(modifier: Modifier = Modifier) {
    val tooltipColor = ChallaTheme.colors.backgroundLevel3

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 위쪽 메뉴를 가리키는 꼬리
        Canvas(modifier = Modifier.size(width = 12.dp, height = 6.dp)) {
            val tail =
                Path().apply {
                    moveTo(size.width / 2f, 0f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
            drawPath(path = tail, color = tooltipColor)
        }

        Text(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(tooltipColor)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            text = stringResource(R.string.gallery_invite_tooltip),
            color = ChallaTheme.colors.labelSubtle,
            maxLines = 1,
            style = ChallaTheme.typography.descriptionLarge.medium,
        )
    }
}

@ComposePreview(showBackground = true, name = "InviteTooltip")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryInviteTooltipPreview() {
    GalleryInviteTooltip()
}
