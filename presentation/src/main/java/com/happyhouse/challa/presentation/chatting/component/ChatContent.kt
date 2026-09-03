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
import androidx.compose.runtime.snapshotFlow
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
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.chatting.contract.ChatState.ChatInfo
import com.happyhouse.challa.presentation.chatting.contract.ChatState.ChatInfo.LoadMoreState
import com.happyhouse.challa.presentation.chatting.model.ChatUiModel
import com.happyhouse.challa.presentation.designsystem.component.ChallaProfileImage
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.reaction.ReactionEmojiSticker
import com.happyhouse.challa.presentation.reaction.labelRes
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.distinctUntilChanged
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
            ChatStatusMessage(
                modifier = modifier.padding(scaffoldPadding),
                message = stringResource(R.string.chat_load_failure),
                actionLabel = stringResource(R.string.chat_retry),
                onAction = onRetry,
            )
        }

        is ChatInfo.Loaded -> {
            if (chatInfo.chats.isEmpty()) {
                ChatStatusMessage(
                    modifier = modifier.padding(scaffoldPadding),
                    message = stringResource(R.string.chat_empty),
                )
            } else {
                ChatList(
                    modifier = modifier.padding(scaffoldPadding),
                    loadedChatInfo = chatInfo,
                    onLoadMore = onLoadMore,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ChatList(
    loadedChatInfo: ChatInfo.Loaded,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val isImeVisible = WindowInsets.isImeVisible
    var hasCompletedInitialScroll by remember { mutableStateOf(false) }
    var lastObservedChatId by remember { mutableStateOf(loadedChatInfo.chats.last().chatId) }
    val statusItemIndexOffset = if (loadedChatInfo.loadMoreState == LoadMoreState.IDLE) 0 else 1
    val shouldLoadMore by
        remember(
            listState,
            loadedChatInfo.chats.size,
            loadedChatInfo.hasNext,
            loadedChatInfo.loadMoreState,
        ) {
            derivedStateOf {
                val firstVisibleIndex = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index
                hasCompletedInitialScroll &&
                    loadedChatInfo.hasNext &&
                    loadedChatInfo.loadMoreState == LoadMoreState.IDLE &&
                    firstVisibleIndex != null &&
                    firstVisibleIndex <= LOAD_MORE_THRESHOLD
            }
        }

    LaunchedEffect(loadedChatInfo.chats.size) {
        if (!hasCompletedInitialScroll && loadedChatInfo.chats.isNotEmpty()) {
            listState.scrollToItem(loadedChatInfo.chats.lastIndex + statusItemIndexOffset)
            hasCompletedInitialScroll = true
        }
    }

    LaunchedEffect(loadedChatInfo.chats.last().chatId) {
        val latestChat = loadedChatInfo.chats.last()
        val latestChatId = latestChat.chatId
        if (hasCompletedInitialScroll && latestChatId != lastObservedChatId) {
            val previousLatestIndex =
                loadedChatInfo.chats.indexOfFirst { chat -> chat.chatId == lastObservedChatId }
            val lastVisibleChatIndex =
                listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index?.minus(
                    statusItemIndexOffset,
                )
            val wasNearBottom =
                previousLatestIndex >= 0 &&
                    lastVisibleChatIndex != null &&
                    lastVisibleChatIndex >= previousLatestIndex - AUTO_SCROLL_THRESHOLD

            if (wasNearBottom || latestChat.isMine) {
                listState.scrollToItem(loadedChatInfo.chats.lastIndex + statusItemIndexOffset)
            }
        }
        lastObservedChatId = latestChatId
    }

    LaunchedEffect(isImeVisible, loadedChatInfo.chats.size) {
        if (!isImeVisible || loadedChatInfo.chats.isEmpty()) return@LaunchedEffect

        snapshotFlow { listState.layoutInfo.viewportSize.height }
            .distinctUntilChanged()
            .collect {
                listState.scrollToItem(loadedChatInfo.chats.lastIndex + statusItemIndexOffset)
            }
    }

    LaunchedEffect(shouldLoadMore, loadedChatInfo.chats.size) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding =
            PaddingValues(
                horizontal = ChatHorizontalPadding,
                vertical = ChatVerticalPadding,
            ),
    ) {
        if (loadedChatInfo.loadMoreState != LoadMoreState.IDLE) {
            item(key = LOAD_MORE_STATUS_ITEM_KEY) {
                when (loadedChatInfo.loadMoreState) {
                    LoadMoreState.IDLE -> Unit
                    LoadMoreState.LOADING -> {
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

                    LoadMoreState.ERROR -> ChatLoadMoreError(onRetry = onLoadMore)
                }
            }
        }

        itemsIndexed(
            items = loadedChatInfo.chats,
            key = { _, chat -> chat.chatId },
        ) { index, chat ->
            val previousChat = loadedChatInfo.chats.getOrNull(index - 1)
            val nextChat = loadedChatInfo.chats.getOrNull(index + 1)
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
                        text = ChatDateHeaderFormatter.format(chat.createdAt),
                    )
                }

                ChatListItem(
                    chat = chat,
                    showsUserName = startsSenderGroup,
                    showsProfileImage = endsSenderGroup,
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
private fun ChatListItem(
    chat: ChatUiModel,
    showsUserName: Boolean,
    showsProfileImage: Boolean,
    modifier: Modifier = Modifier,
) {
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

private const val LOAD_MORE_THRESHOLD = 3
private const val AUTO_SCROLL_THRESHOLD = 1
private const val LOAD_MORE_STATUS_ITEM_KEY = "chat-load-more-status"
private val ChatHorizontalPadding = 20.dp
private val ChatVerticalPadding = 16.dp
private val SameSenderSpacing = 4.dp
private val DifferentSenderSpacing = 24.dp
private val DateHeaderTopSpacing = 24.dp
private val DateHeaderBottomSpacing = 24.dp
private val ChatPhotoWidth = 104.dp
private val ChatPhotoHeight = 140.dp
private val ChatReactionStickerSize = 64.dp
private val ChatReactionStickerOverhang = 32.dp
private val ChatDateHeaderFormatter = DateTimeFormatter.ofPattern("M.d. a h:mm", Locale.KOREA)

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
                        ChatUiModel.Default(
                            chatId = 1L,
                            userId = 1L,
                            content = "강릉에 도착하면 바로 사진 찍으러 가자!",
                            createdAt = previewCreatedAt,
                            isMine = false,
                            userName = "그린그린여성현",
                            userProfileImageUrl = null,
                        ),
                        ChatUiModel.Default(
                            chatId = 2L,
                            userId = 2L,
                            content = "좋아! 바다부터 보고 숙소로 이동하자.",
                            createdAt = previewCreatedAt.plusMinutes(1),
                            isMine = true,
                            userName = "찰나",
                            userProfileImageUrl = null,
                        ),
                        ChatUiModel.Comment(
                            chatId = 3L,
                            userId = 1L,
                            content = "이 사진 분위기 정말 좋다.",
                            photoImageUrl = previewPhotoUrl,
                            createdAt = previewCreatedAt.plusMinutes(2),
                            isMine = false,
                            userName = "그린그린여성현",
                            userProfileImageUrl = null,
                        ),
                        ChatUiModel.Comment(
                            chatId = 4L,
                            userId = 2L,
                            content = "나도 이 사진이 제일 마음에 들어.",
                            photoImageUrl = previewPhotoUrl,
                            createdAt = previewCreatedAt.plusMinutes(3),
                            isMine = true,
                            userName = "찰나",
                            userProfileImageUrl = null,
                        ),
                        ChatUiModel.Emoji(
                            chatId = 5L,
                            userId = 1L,
                            reactionEmoji = ReactionEmoji.POOP,
                            photoImageUrl = previewPhotoUrl,
                            createdAt = previewCreatedAt.plusMinutes(4),
                            isMine = false,
                            userName = "그린그린여성현",
                            userProfileImageUrl = null,
                        ),
                        ChatUiModel.Emoji(
                            chatId = 6L,
                            userId = 2L,
                            reactionEmoji = ReactionEmoji.FIRE,
                            photoImageUrl = previewPhotoUrl,
                            createdAt = previewCreatedAt.plusMinutes(5),
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

@ComposePreview(name = "ChatContent - 추가 페이지 로딩 실패")
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun ChatLoadMoreErrorPreview() {
    ChatLoadMoreError(
        modifier = Modifier.fillMaxWidth(),
        onRetry = {},
    )
}
