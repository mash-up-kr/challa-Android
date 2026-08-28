package com.happyhouse.challa.presentation.chatting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaInputBox
import com.happyhouse.challa.presentation.designsystem.component.ChallaNavigationIconButton
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigation
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigationVariant
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.layout.ChallaScaffold
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.challaBackgroundGlow

private const val TOOLTIP_BACKGROUND_ALPHA = 0.77f

@Composable
fun ChatScreen(
    roomName: String,
    message: String,
    onMessageChange: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(ChallaTheme.colors.backgroundSurface)
                .challaBackgroundGlow(),
    ) {
        ChallaScaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                ChatTopBar(
                    roomName = roomName,
                    onBackClick = onBackClick,
                )
            },
            bottomBar = {
                ChatInputArea(
                    message = message,
                    onMessageChange = onMessageChange,
                )
            },
        ) { innerPadding ->
            Spacer(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
            )
        }
    }
}

@Composable
private fun ChatTopBar(
    roomName: String,
    onBackClick: () -> Unit,
) {
    ChallaTopNavigation(
        title = roomName,
        variant = ChallaTopNavigationVariant.SUB,
        leadingIcon = {
            ChallaNavigationIconButton(
                icon = ChallaIcons.Left,
                onClick = onBackClick,
                contentDescription = stringResource(R.string.chat_back_description),
            )
        },
    )
}

@Composable
private fun ChatInputArea(
    message: String,
    onMessageChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ChatFirstMessageTooltip()

        Spacer(modifier = Modifier.size(7.dp))

        ChallaInputBox(
            value = message,
            onValueChange = onMessageChange,
            placeholder = stringResource(R.string.chat_message_placeholder),
        )
    }
}

@Composable
private fun ChatFirstMessageTooltip(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier =
                Modifier
                    .widthIn(min = 64.dp, max = 256.dp)
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

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun ChatScreenPreview() {
    ChatScreen(
        roomName = "해피하우스 강릉 여행",
        message = "",
        onMessageChange = {},
        onBackClick = {},
    )
}
