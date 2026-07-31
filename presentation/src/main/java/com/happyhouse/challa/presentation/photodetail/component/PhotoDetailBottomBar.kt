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
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

// TODO: 피그마 수치 확인 전까지 쓰는 임시 여백. 디자인 확정되면 맞출 것. (이슈 #62)
private val BottomBarHorizontalPadding = 16.dp
private val BottomBarTopPadding = 8.dp
private val BottomBarBottomPadding = 16.dp
private val BottomBarSpacing = 12.dp

/** 사진 상세 하단의 반응·메시지 영역. 사진이 있을 때만 그린다. */
@Composable
fun PhotoDetailBottomBar(
    message: String,
    isMessageSendable: Boolean,
    onEmojiClick: (ReactionEmoji) -> Unit,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    start = BottomBarHorizontalPadding,
                    end = BottomBarHorizontalPadding,
                    top = BottomBarTopPadding,
                    bottom = BottomBarBottomPadding,
                ),
        verticalArrangement = Arrangement.spacedBy(BottomBarSpacing),
    ) {
        PhotoReactionBar(onEmojiClick = onEmojiClick)

        PhotoMessageInput(
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
        onEmojiClick = {},
        onMessageChange = {},
        onSendClick = {},
    )
}
