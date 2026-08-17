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

private val TooltipCornerRadius = 8.dp
private val TooltipHorizontalPadding = 12.dp
private val TooltipVerticalPadding = 6.dp

/** 위쪽 메뉴를 가리키는 꼬리 */
private val TailWidth = 12.dp
private val TailHeight = 6.dp

/**
 * 초대 메뉴 아래에 붙어 초대 코드 사용법을 알려주는 툴팁
 *
 * 노출 여부는 화면이 정하고, 여기서는 그리기만 한다.
 */
@Composable
fun GalleryInviteTooltip(modifier: Modifier = Modifier) {
    val tooltipColor = ChallaTheme.colors.backgroundLevel3

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(modifier = Modifier.size(width = TailWidth, height = TailHeight)) {
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
                    .clip(RoundedCornerShape(TooltipCornerRadius))
                    .background(tooltipColor)
                    .padding(
                        horizontal = TooltipHorizontalPadding,
                        vertical = TooltipVerticalPadding,
                    ),
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
