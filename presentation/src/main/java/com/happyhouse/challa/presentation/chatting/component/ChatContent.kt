package com.happyhouse.challa.presentation.chatting.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.chatting.contract.ChatState.ChatInfo
import com.happyhouse.challa.presentation.chatting.contract.ChatState.ChatInfo.LoadMoreState
import com.happyhouse.challa.presentation.chatting.model.ChatUiModel
import com.happyhouse.challa.presentation.chatting.previewChats
import com.happyhouse.challa.presentation.designsystem.component.ChallaProgressIndicator
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import kotlinx.collections.immutable.ImmutableList
import java.time.format.DateTimeFormatter
import java.util.Locale
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
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center,
            ) {
                ChallaProgressIndicator()
            }
        }

        ChatInfo.Error -> {
            ChatStatusMessage(
                modifier = modifier,
                message = stringResource(R.string.chat_load_failure),
                actionLabel = stringResource(R.string.chat_retry),
                onAction = onRetry,
            )
        }

        is ChatInfo.Loaded -> {
            if (chatInfo.chats.isEmpty()) {
                ChatStatusMessage(
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
    val leadingStatusItemCount = if (chatInfo.loadMoreState == LoadMoreState.IDLE) 0 else 1

    ChatListSideEffects(
        chatInfo = chatInfo,
        listState = listState,
        leadingStatusItemCount = leadingStatusItemCount,
        onLoadMore = onLoadMore,
    )

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding =
            PaddingValues(
                horizontal = ChatHorizontalPadding,
                vertical = ChatVerticalPadding,
            ),
    ) {
        if (chatInfo.loadMoreState != LoadMoreState.IDLE) {
            item(key = LOAD_MORE_STATUS_ITEM_KEY) {
                when (chatInfo.loadMoreState) {
                    LoadMoreState.IDLE -> Unit
                    LoadMoreState.LOADING -> {
                        Box(
                            modifier =
                                Modifier
                                    .fillParentMaxWidth()
                                    .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            ChallaProgressIndicator(
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }

                    LoadMoreState.ERROR -> ChatLoadMoreError(onRetry = onLoadMore)
                }
            }
        }

        itemsIndexed(
            items = chatInfo.chats,
            key = { _, chat -> chat.chatId },
        ) { index, chat ->
            val layoutInfo = chatInfo.chats.itemLayoutInfoAt(index)

            Column(modifier = Modifier.padding(top = layoutInfo.topPadding)) {
                if (layoutInfo.showDateHeader) {
                    ChatDateHeader(
                        modifier = Modifier.padding(bottom = DateHeaderBottomSpacing),
                        text = ChatDateHeaderFormatter.format(chat.createdAt),
                    )
                }

                ChatMessageItem(
                    chat = chat,
                    showSenderName = layoutInfo.showSenderName,
                    showSenderProfileImage = layoutInfo.showSenderProfileImage,
                )
            }
        }
    }
}

@Composable
private fun ChatLoadMoreError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.chat_load_more_failure),
            color = ChallaTheme.colors.labelNormal,
            style = ChallaTheme.typography.bodySmall.medium,
        )
        TextButton(onClick = onRetry) {
            Text(
                text = stringResource(R.string.chat_retry),
                color = ChallaTheme.colors.primary,
                style = ChallaTheme.typography.bodyMedium.bold,
            )
        }
    }
}

@Composable
private fun ChatDateHeader(
    text: String,
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
            text = text,
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
private fun ChatStatusMessage(
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

private data class ChatItemLayoutInfo(
    val showDateHeader: Boolean,
    val showSenderName: Boolean,
    val showSenderProfileImage: Boolean,
    val topPadding: Dp,
)

private fun ImmutableList<ChatUiModel>.itemLayoutInfoAt(index: Int): ChatItemLayoutInfo {
    val chat = get(index)
    val previousChat = getOrNull(index - 1)
    val nextChat = getOrNull(index + 1)
    val showDateHeader =
        previousChat == null || previousChat.createdAt.toLocalDate() != chat.createdAt.toLocalDate()
    val showSenderName = showDateHeader || previousChat.userId != chat.userId
    val showSenderProfileImage =
        nextChat == null ||
            nextChat.userId != chat.userId ||
            nextChat.createdAt.toLocalDate() != chat.createdAt.toLocalDate()
    val topPadding =
        when {
            index == 0 -> 0.dp
            showDateHeader -> DateHeaderTopSpacing
            previousChat.userId == chat.userId -> SameSenderTopSpacing
            else -> DifferentSenderTopSpacing
        }

    return ChatItemLayoutInfo(
        showDateHeader = showDateHeader,
        showSenderName = showSenderName,
        showSenderProfileImage = showSenderProfileImage,
        topPadding = topPadding,
    )
}

private const val LOAD_MORE_STATUS_ITEM_KEY = "chat-load-more-status"
private val ChatHorizontalPadding = 20.dp
private val ChatVerticalPadding = 16.dp
private val SameSenderTopSpacing = 4.dp
private val DifferentSenderTopSpacing = 24.dp
private val DateHeaderTopSpacing = 24.dp
private val DateHeaderBottomSpacing = 24.dp
private val ChatDateHeaderFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 EEEE", Locale.KOREA)

@ComposePreview(name = "ChatContent - 채팅 목록")
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun ChatContentLoadedPreview() {
    val previewPhotoUrl =
        "android.resource://${LocalContext.current.packageName}/${R.drawable.img_onboarding_1}"

    ChatContent(
        modifier = Modifier.fillMaxSize(),
        chatInfo =
            ChatInfo.Loaded(
                chats = previewChats(photoImageUrl = previewPhotoUrl),
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

@ComposePreview(name = "ChatContent - 추가 페이지 로딩 실패")
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun ChatLoadMoreErrorPreview() {
    ChatLoadMoreError(
        modifier = Modifier.fillMaxWidth(),
        onRetry = {},
    )
}
