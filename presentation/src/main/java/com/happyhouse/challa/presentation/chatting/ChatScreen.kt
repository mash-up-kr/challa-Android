package com.happyhouse.challa.presentation.chatting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.chatting.component.ChatContent
import com.happyhouse.challa.presentation.chatting.component.ChatInputArea
import com.happyhouse.challa.presentation.chatting.component.ChatTopBar
import com.happyhouse.challa.presentation.chatting.contract.ChatIntent
import com.happyhouse.challa.presentation.chatting.contract.ChatState
import com.happyhouse.challa.presentation.chatting.contract.ChatState.ChatInfo
import com.happyhouse.challa.presentation.chatting.model.ChatUiModel
import com.happyhouse.challa.presentation.designsystem.layout.ChallaScaffold
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.challaBackgroundGlow
import kotlinx.collections.immutable.persistentListOf
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun ChatScreen(
    state: ChatState,
    snackbarHostState: SnackbarHostState,
    onIntent: (ChatIntent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChallaScaffold(
        modifier =
            modifier
                .fillMaxSize()
                .background(ChallaTheme.colors.backgroundSurface)
                .challaBackgroundGlow()
                .imePadding(),
        containerColor = Color.Transparent,
        snackbarHostState = snackbarHostState,
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
                onSend = { onIntent(ChatIntent.MessageSend) },
            )
        },
    ) { innerPadding ->
        ChatContent(
            modifier = Modifier.fillMaxSize(),
            chatInfo = state.chatInfo,
            onRetry = { onIntent(ChatIntent.ChatsLoad) },
            onLoadMore = { onIntent(ChatIntent.ChatsLoadMore) },
            scaffoldPadding = innerPadding,
        )
    }
}

@Preview(name = "ChatScreen - 채팅 목록")
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun ChatScreenPreview() {
    val previewPhotoUrl =
        "android.resource://${LocalContext.current.packageName}/${R.drawable.img_onboarding_1}"
    val previewZoneId = ZoneId.systemDefault()

    ChatScreen(
        state =
            ChatState(
                roomName = "해피하우스 강릉 여행",
                chatInfo =
                    ChatInfo.Loaded(
                        chats =
                            persistentListOf(
                                ChatUiModel.Default(
                                    chatId = 1L,
                                    userId = 1L,
                                    content = "강릉에 도착하면 바로 사진 찍으러 가자!",
                                    createdAt = ZonedDateTime.of(2026, 8, 29, 20, 15, 0, 0, previewZoneId),
                                    isMine = false,
                                    userName = "user1",
                                    userProfileImageUrl = null,
                                ),
                                ChatUiModel.Comment(
                                    chatId = 2L,
                                    userId = 2L,
                                    content = "좋아! 바다부터 보고 숙소로 이동하자.",
                                    photoImageUrl = previewPhotoUrl,
                                    createdAt = ZonedDateTime.of(2026, 8, 29, 20, 17, 0, 0, previewZoneId),
                                    isMine = true,
                                    userName = "찰나",
                                    userProfileImageUrl = null,
                                ),
                                ChatUiModel.Emoji(
                                    chatId = 3L,
                                    userId = 3L,
                                    reactionEmoji = ReactionEmoji.FIRE,
                                    photoImageUrl = previewPhotoUrl,
                                    createdAt = ZonedDateTime.of(2026, 8, 30, 9, 34, 0, 0, previewZoneId),
                                    isMine = false,
                                    userName = "여름여행가자",
                                    userProfileImageUrl = null,
                                ),
                            ),
                    ),
            ),
        snackbarHostState = remember { SnackbarHostState() },
        onIntent = {},
        onBackClick = {},
    )
}
