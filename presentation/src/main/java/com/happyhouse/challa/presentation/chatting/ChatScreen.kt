package com.happyhouse.challa.presentation.chatting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.happyhouse.challa.presentation.chatting.component.ChatInputArea
import com.happyhouse.challa.presentation.chatting.component.ChatTopBar
import com.happyhouse.challa.presentation.chatting.contract.ChatIntent
import com.happyhouse.challa.presentation.chatting.contract.ChatState
import com.happyhouse.challa.presentation.designsystem.layout.ChallaScaffold
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.challaBackgroundGlow

@Composable
fun ChatScreen(
    state: ChatState,
    onIntent: (ChatIntent) -> Unit,
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
                    roomName = state.roomName,
                    onBackClick = onBackClick,
                )
            },
            bottomBar = {
                ChatInputArea(
                    message = state.message,
                    showsFirstMessageTooltip = state.showsFirstMessageTooltip,
                    onMessageChange = { message ->
                        onIntent(ChatIntent.MessageChange(message))
                    },
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

@Preview
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun ChatScreenPreview() {
    ChatScreen(
        state = ChatState(roomName = "해피하우스 강릉 여행"),
        onIntent = {},
        onBackClick = {},
    )
}
