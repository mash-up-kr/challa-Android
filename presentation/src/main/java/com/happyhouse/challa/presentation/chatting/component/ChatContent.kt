package com.happyhouse.challa.presentation.chatting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.happyhouse.challa.domain.model.chat.ChatType
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.chatting.contract.ChatState.ChatInfo
import com.happyhouse.challa.presentation.chatting.model.ChatUiModel
import com.happyhouse.challa.presentation.designsystem.component.ChallaProfileImage
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import kotlinx.collections.immutable.persistentListOf
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
fun ChatContent(
    chatInfo: ChatInfo,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (chatInfo) {
        ChatInfo.Loading -> {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ChallaTheme.colors.labelNormal)
            }
        }

        ChatInfo.Error -> {
            ChatListMessage(
                modifier = modifier,
                message = stringResource(R.string.chat_load_failure),
                actionLabel = stringResource(R.string.chat_retry),
                onAction = onRetry,
            )
        }

        is ChatInfo.Loaded -> {
            if (chatInfo.chats.isEmpty()) {
                ChatListMessage(
                    modifier = modifier,
                    message = stringResource(R.string.chat_empty),
                )
            } else {
                ChatList(
                    modifier = modifier,
                    chatInfo = chatInfo,
                    onLoadMore = onLoadMore,
                )
            }
        }
    }
}

@Composable
private fun ChatList(
    chatInfo: ChatInfo.Loaded,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by
        remember(listState, chatInfo.chats.size, chatInfo.hasNext) {
            derivedStateOf {
                val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                chatInfo.hasNext && lastVisibleIndex >= chatInfo.chats.lastIndex - LOAD_MORE_THRESHOLD
            }
        }

    LaunchedEffect(shouldLoadMore, chatInfo.chats.size) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(chatInfo.chats) { chat ->
            ChatListItem(chat = chat)
        }

        if (chatInfo.isLoadingMore) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxWidth().padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = ChallaTheme.colors.labelNormal,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatListItem(
    chat: ChatUiModel,
    modifier: Modifier = Modifier,
) {
    val bubbleShape =
        if (chat.isMine) {
            RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
        } else {
            RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
        }
    val bubbleColor = if (chat.isMine) ChallaTheme.colors.staticWhite else ChallaTheme.colors.backgroundLevel2
    val contentColor = if (chat.isMine) ChallaTheme.colors.staticBlack else ChallaTheme.colors.labelNormal

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (!chat.isMine) {
            ChallaProfileImage(
                modifier = Modifier.size(36.dp),
                profileImageUrl = chat.userProfileImageUrl,
                backgroundColor = ChallaTheme.colors.backgroundLevel2,
                fallbackIconTint = ChallaTheme.colors.lineNeutral,
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = if (chat.isMine) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (!chat.isMine) {
                chat.userName?.takeIf(String::isNotBlank)?.let { userName ->
                    Text(
                        text = userName,
                        color = ChallaTheme.colors.labelSubtle,
                        style = ChallaTheme.typography.descriptionLarge.medium,
                    )
                }
            }

            Column(
                modifier =
                    Modifier
                        .widthIn(max = 280.dp)
                        .clip(bubbleShape)
                        .background(bubbleColor)
                        .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                chat.photoImageUrl?.takeIf(String::isNotBlank)?.let { imageUrl ->
                    AsyncImage(
                        modifier =
                            Modifier
                                .widthIn(max = 220.dp)
                                .height(132.dp)
                                .clip(RoundedCornerShape(10.dp)),
                        model =
                            ImageRequest
                                .Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                        contentDescription = stringResource(R.string.chat_photo_description),
                        contentScale = ContentScale.Crop,
                    )
                }

                Text(
                    text = chat.content,
                    color = contentColor,
                    style = ChallaTheme.typography.bodyMedium.medium,
                )
            }
        }
    }
}

@Composable
private fun ChatListMessage(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            color = ChallaTheme.colors.labelNormal,
            style = ChallaTheme.typography.bodyMedium.medium,
        )

        actionLabel?.let { label ->
            TextButton(onClick = onAction) {
                Text(
                    text = label,
                    color = ChallaTheme.colors.primary,
                    style = ChallaTheme.typography.bodyMedium.bold,
                )
            }
        }
    }
}

private const val LOAD_MORE_THRESHOLD = 3

@ComposePreview(name = "ChatContent - 채팅 목록")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChatContentLoadedPreview() {
    ChatContent(
        modifier = Modifier.fillMaxSize(),
        chatInfo =
            ChatInfo.Loaded(
                chats =
                    persistentListOf(
                        ChatUiModel(
                            type = ChatType.DEFAULT,
                            content = "강릉에 도착하면 바로 사진 찍으러 가자!",
                            photoImageUrl = null,
                            isMine = false,
                            userName = "그린그린여성현",
                            userProfileImageUrl = null,
                        ),
                        ChatUiModel(
                            type = ChatType.COMMENT,
                            content = "이 사진 분위기 정말 좋다.",
                            photoImageUrl = null,
                            isMine = true,
                            userName = "찰나",
                            userProfileImageUrl = null,
                        ),
                    ),
            ),
        onRetry = {},
        onLoadMore = {},
    )
}

@ComposePreview(name = "ChatContent - 빈 목록")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChatContentEmptyPreview() {
    ChatContent(
        modifier = Modifier.fillMaxSize(),
        chatInfo = ChatInfo.Loaded(),
        onRetry = {},
        onLoadMore = {},
    )
}

@ComposePreview(name = "ChatContent - Loading")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChatContentLoadingPreview() {
    ChatContent(
        modifier = Modifier.fillMaxSize(),
        chatInfo = ChatInfo.Loading,
        onRetry = {},
        onLoadMore = {},
    )
}

@ComposePreview(name = "ChatContent - Error")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChatContentErrorPreview() {
    ChatContent(
        modifier = Modifier.fillMaxSize(),
        chatInfo = ChatInfo.Error,
        onRetry = {},
        onLoadMore = {},
    )
}
