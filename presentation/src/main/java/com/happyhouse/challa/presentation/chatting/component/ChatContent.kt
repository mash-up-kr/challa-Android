package com.happyhouse.challa.presentation.chatting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.chatting.contract.ChatState.ChatInfo
import com.happyhouse.challa.presentation.chatting.contract.ChatState.ChatInfo.LoadMoreState
import com.happyhouse.challa.presentation.chatting.model.ChatUiModel
import com.happyhouse.challa.presentation.chatting.previewChats
import com.happyhouse.challa.presentation.designsystem.component.ChallaProfileImage
import com.happyhouse.challa.presentation.designsystem.component.ChallaProgressIndicator
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.reaction.ReactionEmojiSticker
import com.happyhouse.challa.presentation.reaction.labelRes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.distinctUntilChanged
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
@OptIn(ExperimentalLayoutApi::class)
private fun ChatListSideEffects(
    chatInfo: ChatInfo.Loaded,
    listState: LazyListState,
    leadingStatusItemCount: Int,
    onLoadMore: () -> Unit,
) {
    val chats = chatInfo.chats
    val isImeVisible = WindowInsets.isImeVisible
    var hasCompletedInitialScroll by rememberSaveable { mutableStateOf(false) }
    var previousLatestChatId by remember { mutableLongStateOf(chats.last().chatId) }
    val scrollTargetIndex = chats.lastIndex + leadingStatusItemCount
    val latestScrollTargetIndex by rememberUpdatedState(scrollTargetIndex)
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    val shouldLoadPreviousPage by
        remember(
            listState,
            chats.size,
            chatInfo.hasNext,
            chatInfo.loadMoreState,
        ) {
            derivedStateOf {
                val firstVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index
                hasCompletedInitialScroll &&
                    chatInfo.hasNext &&
                    chatInfo.loadMoreState == LoadMoreState.IDLE &&
                    firstVisibleItemIndex != null &&
                    firstVisibleItemIndex <= LOAD_MORE_TRIGGER_INDEX
            }
        }

    LaunchedEffect(chats.size) {
        if (!hasCompletedInitialScroll) {
            listState.scrollToItem(latestScrollTargetIndex)
            hasCompletedInitialScroll = true
        }
    }

    LaunchedEffect(chats.last().chatId) {
        val latestChat = chats.last()
        val latestChatId = latestChat.chatId
        if (hasCompletedInitialScroll && latestChatId != previousLatestChatId) {
            val previousLatestChatIndex =
                chats.indexOfFirst { chat -> chat.chatId == previousLatestChatId }
            val lastVisibleChatIndex =
                listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index?.minus(
                    leadingStatusItemCount,
                )
            val wasNearBottom =
                previousLatestChatIndex >= 0 &&
                    lastVisibleChatIndex != null &&
                    lastVisibleChatIndex >= previousLatestChatIndex - AUTO_SCROLL_ITEM_THRESHOLD

            if (wasNearBottom || latestChat.isMine) {
                listState.scrollToItem(latestScrollTargetIndex)
            }
        }
        previousLatestChatId = latestChatId
    }

    LaunchedEffect(isImeVisible, chats.size) {
        if (!isImeVisible) return@LaunchedEffect

        snapshotFlow { listState.layoutInfo.viewportSize.height }
            .distinctUntilChanged()
            .collect {
                listState.scrollToItem(latestScrollTargetIndex)
            }
    }

    LaunchedEffect(shouldLoadPreviousPage, chats.size) {
        if (shouldLoadPreviousPage) currentOnLoadMore()
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
private fun ChatMessageItem(
    chat: ChatUiModel,
    showSenderName: Boolean,
    showSenderProfileImage: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!chat.isMine) {
            if (showSenderProfileImage) {
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
            if (!chat.isMine && showSenderName) {
                chat.userName?.takeIf(String::isNotBlank)?.let { userName ->
                    Text(
                        text = userName,
                        color = ChallaTheme.colors.labelNeutral,
                        style = ChallaTheme.typography.bodyXSmall.medium,
                    )
                }
            }

            when (chat) {
                is ChatUiModel.Default -> {
                    ChatMessageBubble(
                        content = chat.content,
                        isMine = chat.isMine,
                    )
                }

                is ChatUiModel.Emoji -> {
                    ChatPhoto(
                        imageUrl = chat.photoImageUrl,
                        reactionEmoji = chat.reactionEmoji,
                        isMine = chat.isMine,
                    )
                }

                is ChatUiModel.Comment -> {
                    ChatPhoto(
                        imageUrl = chat.photoImageUrl,
                        reactionEmoji = null,
                        isMine = chat.isMine,
                    )
                    ChatMessageBubble(
                        content = chat.content,
                        isMine = chat.isMine,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    content: String,
    isMine: Boolean,
    modifier: Modifier = Modifier,
) {
    if (content.isBlank()) return

    Text(
        modifier =
            modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isMine) ChallaTheme.colors.staticWhite else ChallaTheme.colors.backgroundLevel4,
                ).padding(horizontal = 12.dp, vertical = 8.dp),
        text = content,
        color = if (isMine) ChallaTheme.colors.staticBlack else ChallaTheme.colors.labelNormal,
        style = ChallaTheme.typography.bodyMedium.medium,
    )
}

@Composable
private fun ChatPhoto(
    imageUrl: String,
    reactionEmoji: ReactionEmoji?,
    isMine: Boolean,
    modifier: Modifier = Modifier,
) {
    if (imageUrl.isBlank()) return

    if (reactionEmoji == null) {
        ChatPhotoImage(
            modifier = modifier,
            imageUrl = imageUrl,
        )
        return
    }

    Box(
        modifier =
            modifier.size(
                width = ChatPhotoWidth + ChatReactionStickerOverhang,
                height = ChatPhotoHeight,
            ),
    ) {
        ChatPhotoImage(
            modifier =
                Modifier.align(
                    if (isMine) Alignment.CenterEnd else Alignment.CenterStart,
                ),
            imageUrl = imageUrl,
        )
        ReactionEmojiSticker(
            modifier =
                Modifier
                    .align(if (isMine) Alignment.CenterStart else Alignment.CenterEnd)
                    .size(ChatReactionStickerSize),
            emoji = reactionEmoji,
            contentDescription =
                stringResource(
                    R.string.chat_reaction_emoji_description,
                    stringResource(reactionEmoji.labelRes),
                ),
        )
    }
}

@Composable
private fun ChatPhotoImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val imageRequest =
        remember(context, imageUrl) {
            ImageRequest
                .Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .build()
        }

    AsyncImage(
        modifier =
            modifier
                .size(width = ChatPhotoWidth, height = ChatPhotoHeight)
                .clip(RoundedCornerShape(10.dp)),
        model = imageRequest,
        contentDescription = stringResource(R.string.chat_photo_description),
        contentScale = ContentScale.Crop,
    )
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

private const val LOAD_MORE_TRIGGER_INDEX = 3
private const val AUTO_SCROLL_ITEM_THRESHOLD = 1
private const val LOAD_MORE_STATUS_ITEM_KEY = "chat-load-more-status"
private val ChatHorizontalPadding = 20.dp
private val ChatVerticalPadding = 16.dp
private val SameSenderTopSpacing = 4.dp
private val DifferentSenderTopSpacing = 24.dp
private val DateHeaderTopSpacing = 24.dp
private val DateHeaderBottomSpacing = 24.dp
private val ChatPhotoWidth = 104.dp
private val ChatPhotoHeight = 140.dp
private val ChatReactionStickerSize = 64.dp
private val ChatReactionStickerOverhang = 32.dp
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
