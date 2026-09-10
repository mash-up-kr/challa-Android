package com.happyhouse.challa.presentation.chatting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.chatting.model.ChatUiModel
import com.happyhouse.challa.presentation.designsystem.component.ChallaProfileImage
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.reaction.ReactionEmojiSticker
import com.happyhouse.challa.presentation.reaction.labelRes

/**
 * 메시지 유형에 따라 말풍선, 사진, 리액션을 표시한다.
 *
 * 발신자 이름과 프로필의 표시 여부는 호출부에서 날짜·발신자 그룹 경계를 계산해 전달한다.
 * 내 메시지는 두 플래그와 관계없이 이름과 프로필을 숨기고, 상대 프로필을 숨길 때는 자리를 유지한다.
 */
@Composable
internal fun ChatMessageItem(
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

private val ChatPhotoWidth = 104.dp
private val ChatPhotoHeight = 140.dp
private val ChatReactionStickerSize = 64.dp
private val ChatReactionStickerOverhang = 32.dp
