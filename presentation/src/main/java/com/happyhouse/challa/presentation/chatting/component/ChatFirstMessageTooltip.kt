package com.happyhouse.challa.presentation.chatting.component

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

private const val TOOLTIP_BACKGROUND_ALPHA = 0.77f

@Composable
fun ChatFirstMessageTooltip(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(ChallaTheme.colors.backgroundLevel2.copy(alpha = TOOLTIP_BACKGROUND_ALPHA))
                    .padding(10.dp),
            text = stringResource(R.string.chat_first_message_tooltip),
            color = ChallaTheme.colors.labelNormal,
            style = ChallaTheme.typography.descriptionLarge.medium,
        )

        Image(
            modifier =
                Modifier
                    .size(width = 20.dp, height = 8.dp)
                    .rotate(180f),
            painter = painterResource(ChallaIcons.ArrowTip),
            contentDescription = null,
        )
    }
}

@Preview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChatFirstMessageTooltipPreview() {
    ChatFirstMessageTooltip()
}
