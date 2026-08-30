package com.happyhouse.challa.presentation.chatting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import kotlinx.collections.immutable.persistentListOf
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
fun ChatContent(
    chatInfo: ChatInfo,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    scaffoldPadding: PaddingValues = PaddingValues(0.dp),
) {
    when (chatInfo) {
        ChatInfo.Loading -> {
            Box(
                modifier = modifier.padding(scaffoldPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ChallaTheme.colors.primary)
            }
        }

        ChatInfo.Error -> {
            ChatListMessage(
                modifier = modifier.padding(scaffoldPadding),
                message = stringResource(R.string.chat_load_failure),
                actionLabel = stringResource(R.string.chat_retry),
                onAction = onRetry,
            )
        }

        is ChatInfo.Loaded -> {
            if (chatInfo.chats.isEmpty()) {
                ChatListMessage(
                    modifier = modifier.padding(scaffoldPadding),
                    message = stringResource(R.string.chat_empty),
                )
            } else {
                ChatList(
                    modifier = modifier.padding(scaffoldPadding),
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
    var hasCompletedInitialScroll by remember { mutableStateOf(false) }
    val shouldLoadMore by
        remember(listState, chatInfo.chats.size, chatInfo.hasNext) {
            derivedStateOf {
                val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                chatInfo.hasNext && lastVisibleIndex >= chatInfo.chats.lastIndex - LOAD_MORE_THRESHOLD
            }
        }

    LaunchedEffect(chatInfo.chats.size) {
        if (!hasCompletedInitialScroll && chatInfo.chats.isNotEmpty()) {
            listState.scrollToItem(chatInfo.chats.lastIndex)
            hasCompletedInitialScroll = true
        }
    }

    LaunchedEffect(shouldLoadMore, chatInfo.chats.size) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(horizontal = ChatHorizontalPadding, vertical = ChatVerticalPadding),
    ) {
        itemsIndexed(chatInfo.chats) { index, chat ->
            val previousChat = chatInfo.chats.getOrNull(index - 1)
            val nextChat = chatInfo.chats.getOrNull(index + 1)
            val showsDateHeader =
                previousChat == null || previousChat.createdAt.toLocalDate() != chat.createdAt.toLocalDate()
            val startsSenderGroup =
                showsDateHeader || previousChat.userId != chat.userId
            val endsSenderGroup =
                nextChat == null ||
                    nextChat.userId != chat.userId ||
                    nextChat.createdAt.toLocalDate() != chat.createdAt.toLocalDate()
            val topSpacing =
                when {
                    index == 0 -> 0.dp
                    showsDateHeader -> DateHeaderTopSpacing
                    previousChat.userId == chat.userId -> SameSenderSpacing
                    else -> DifferentSenderSpacing
                }

            Column(modifier = Modifier.padding(top = topSpacing)) {
                if (showsDateHeader) {
                    ChatDateHeader(
                        modifier = Modifier.padding(bottom = DateHeaderBottomSpacing),
                        createdAt = chat.createdAt,
                    )
                }

                ChatListItem(
                    chat = chat,
                    showsUserName = startsSenderGroup,
                    showsProfileImage = endsSenderGroup,
                )
            }
        }

        if (chatInfo.isLoadingMore) {
            item {
                Box(
                    modifier =
                        Modifier
                            .fillParentMaxWidth()
                            .padding(vertical = 8.dp),
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
private fun ChatDateHeader(
    createdAt: ZonedDateTime,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = ChallaTheme.colors.lineNeutral,
        )
        Text(
            text = ChatDateFormatter.format(createdAt),
            color = ChallaTheme.colors.labelNeutral,
            style = ChallaTheme.typography.bodySmall.medium,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = ChallaTheme.colors.lineNeutral,
        )
    }
}

@Composable
private fun ChatListItem(
    chat: ChatUiModel,
    showsUserName: Boolean,
    showsProfileImage: Boolean,
    modifier: Modifier = Modifier,
) {
    val bubbleColor =
        if (chat.isMine) ChallaTheme.colors.staticWhite else ChallaTheme.colors.backgroundLevel4
    val contentColor =
        if (chat.isMine) ChallaTheme.colors.staticBlack else ChallaTheme.colors.labelNormal

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!chat.isMine) {
            if (showsProfileImage) {
                ChallaProfileImage(
                    modifier = Modifier.size(22.dp),
                    profileImageUrl = chat.userProfileImageUrl,
                )
            } else {
                Spacer(modifier = Modifier.size(22.dp))
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = if (chat.isMine) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (!chat.isMine && showsUserName) {
                chat.userName?.takeIf(String::isNotBlank)?.let { userName ->
                    Text(
                        text = userName,
                        color = ChallaTheme.colors.labelNeutral,
                        style = ChallaTheme.typography.bodyXSmall.medium,
                    )
                }
            }

            chat.photoImageUrl?.takeIf(String::isNotBlank)?.let { imageUrl ->
                AsyncImage(
                    modifier =
                        Modifier
                            .size(width = 104.dp, height = 140.dp)
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

            chat.content.takeIf(String::isNotBlank)?.let { content ->
                Text(
                    modifier =
                        Modifier
                            .widthIn(max = 280.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bubbleColor)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    text = content,
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
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
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
private val ChatHorizontalPadding = 20.dp
private val ChatVerticalPadding = 16.dp
private val SameSenderSpacing = 4.dp
private val DifferentSenderSpacing = 24.dp
private val DateHeaderTopSpacing = 24.dp
private val DateHeaderBottomSpacing = 24.dp
private val ChatDateFormatter = DateTimeFormatter.ofPattern("M.d. a h:mm", Locale.KOREA)

@ComposePreview(name = "ChatContent - 채팅 목록")
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun ChatContentLoadedPreview() {
    val previewPhotoUrl =
        "android.resource://${LocalContext.current.packageName}/${R.drawable.img_onboarding_1}"
    val previewCreatedAt =
        ZonedDateTime.of(2026, 8, 30, 14, 48, 0, 0, ZoneId.systemDefault())

    ChatContent(
        modifier = Modifier.fillMaxSize(),
        chatInfo =
            ChatInfo.Loaded(
                chats =
                    persistentListOf(
                        ChatUiModel(
                            userId = 1L,
                            type = ChatType.DEFAULT,
                            content = "강릉에 도착하면 바로 사진 찍으러 가자!",
                            photoImageUrl = null,
                            createdAt = previewCreatedAt,
                            isMine = false,
                            userName = "그린그린여성현",
                            userProfileImageUrl = null,
                        ),
                        ChatUiModel(
                            userId = 2L,
                            type = ChatType.COMMENT,
                            content = "이 사진 분위기 정말 좋다.",
                            photoImageUrl = previewPhotoUrl,
                            createdAt = previewCreatedAt.plusMinutes(1),
                            isMine = true,
                            userName = "찰나",
                            userProfileImageUrl = null,
                        ),
                        ChatUiModel(
                            userId = 2L,
                            type = ChatType.COMMENT,
                            content = "이 사진 분위기 정말 좋다.",
                            photoImageUrl = null,
                            createdAt = previewCreatedAt.plusMinutes(2),
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
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun ChatContentEmptyPreview() {
    ChatContent(
        chatInfo = ChatInfo.Loaded(),
        onRetry = {},
        onLoadMore = {},
        modifier = Modifier.fillMaxSize(),
    )
}

@ComposePreview(name = "ChatContent - Loading")
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun ChatContentLoadingPreview() {
    ChatContent(
        chatInfo = ChatInfo.Loading,
        onRetry = {},
        onLoadMore = {},
        modifier = Modifier.fillMaxSize(),
    )
}

@ComposePreview(name = "ChatContent - Error")
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun ChatContentErrorPreview() {
    ChatContent(
        chatInfo = ChatInfo.Error,
        onRetry = {},
        onLoadMore = {},
        modifier = Modifier.fillMaxSize(),
    )
}
