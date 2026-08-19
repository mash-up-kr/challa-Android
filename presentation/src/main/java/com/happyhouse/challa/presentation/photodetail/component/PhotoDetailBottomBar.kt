package com.happyhouse.challa.presentation.photodetail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.photodetail.contract.ReactionEmoji
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private val BottomBarTopPadding = 8.dp
private val BottomBarSpacing = 16.dp

/** 반응 바는 페이지가 화면 끝까지 닿아야 해서, 좌우 여백을 바깥이 아닌 각 항목이 갖는다. */
private val MessageInputHorizontalPadding = 20.dp

@Composable
fun PhotoDetailBottomBar(
    message: String,
    isMessageSendable: Boolean,
    addedEmojis: ImmutableSet<ReactionEmoji>,
    onEmojiClick: (ReactionEmoji) -> Unit,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = BottomBarTopPadding),
        verticalArrangement = Arrangement.spacedBy(BottomBarSpacing),
    ) {
        PhotoReactionBar(
            addedEmojis = addedEmojis,
            onEmojiClick = onEmojiClick,
        )

        PhotoMessageInput(
            modifier = Modifier.padding(horizontal = MessageInputHorizontalPadding),
            message = message,
            isSendable = isMessageSendable,
            onMessageChange = onMessageChange,
            onSendClick = onSendClick,
        )
    }
}

@ComposePreview(showBackground = true, widthDp = 390)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailBottomBarPreview() {
    PhotoDetailBottomBar(
        message = "",
        isMessageSendable = false,
        addedEmojis = persistentSetOf(),
        onEmojiClick = {},
        onMessageChange = {},
        onSendClick = {},
    )
}

@ComposePreview(showBackground = true, widthDp = 390, name = "PhotoDetailBottomBar - 입력됨")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailBottomBarTypingPreview() {
    PhotoDetailBottomBar(
        message = "기엽다",
        isMessageSendable = true,
        addedEmojis = persistentSetOf(ReactionEmoji.FIRE, ReactionEmoji.HEART),
        onEmojiClick = {},
        onMessageChange = {},
        onSendClick = {},
    )
}
