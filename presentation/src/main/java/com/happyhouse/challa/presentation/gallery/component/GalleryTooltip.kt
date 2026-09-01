package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private const val TOOLTIP_BACKGROUND_ALPHA = 0.77f

/**
 * 위쪽을 가리키는 꼬리가 달린 안내 툴팁
 *
 * @param text 툴팁에 그릴 문구
 */
@Composable
fun GalleryTooltip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier.size(width = 20.dp, height = 8.dp),
            painter = painterResource(ChallaIcons.ArrowTip),
            contentDescription = null,
        )

        Text(
            modifier =
                Modifier
                    .widthIn(min = 64.dp, max = 256.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ChallaTheme.colors.backgroundLevel2.copy(alpha = TOOLTIP_BACKGROUND_ALPHA))
                    .padding(10.dp),
            text = text,
            color = ChallaTheme.colors.labelNormal,
            style = ChallaTheme.typography.descriptionLarge.medium,
        )
    }
}

@ComposePreview(showBackground = true, name = "Tooltip - 초대 안내")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryTooltipInvitePreview() {
    GalleryTooltip(text = stringResource(R.string.gallery_invite_tooltip))
}

@ComposePreview(showBackground = true, name = "Tooltip - 필름 당김 안내")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryTooltipPullPreview() {
    GalleryTooltip(text = stringResource(R.string.gallery_print_animation_pull_hint))
}
