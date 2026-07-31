package com.happyhouse.challa.presentation.photodetail.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.clickOnce
import com.happyhouse.challa.presentation.photodetail.contract.ReactionEmoji
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

// TODO: 피그마 수치 확인 전까지 쓰는 임시 값. 디자인 확정되면 맞출 것. (이슈 #62)
private val ReactionButtonSize = 44.dp
private val ReactionEmojiSize = 24.dp

/** 사진에 남길 이모지를 고르는 반응 바. */
@Composable
fun PhotoReactionBar(
    onEmojiClick: (ReactionEmoji) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ReactionEmoji.entries.forEach { emoji ->
            ReactionButton(
                emoji = emoji,
                onClick = { onEmojiClick(emoji) },
            )
        }
    }
}

@Composable
private fun ReactionButton(
    emoji: ReactionEmoji,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(ReactionButtonSize)
                .clip(CircleShape)
                .background(ChallaTheme.colors.backgroundLevel2)
                .clickOnce(
                    role = Role.Button,
                    onClickLabel = stringResource(emoji.contentDescriptionRes),
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        ReactionEmojiImage(
            modifier = Modifier.size(ReactionEmojiSize),
            emoji = emoji,
        )
    }
}

/** 이모지는 여러 색을 그대로 살려야 해서 tint 없이 Image로 그린다. 클릭 라벨은 부모가 붙인다. */
@Composable
internal fun ReactionEmojiImage(
    emoji: ReactionEmoji,
    modifier: Modifier = Modifier,
) {
    Image(
        modifier = modifier,
        painter = painterResource(id = emoji.drawableRes),
        contentDescription = null,
    )
}

internal val ReactionEmoji.drawableRes: Int
    @DrawableRes
    get() =
        when (this) {
            ReactionEmoji.MEDAL -> R.drawable.img_reaction_medal
            ReactionEmoji.HEART -> R.drawable.img_reaction_heart
            ReactionEmoji.POOP -> R.drawable.img_reaction_poop
            ReactionEmoji.CLAP -> R.drawable.img_reaction_clap
            ReactionEmoji.SKULL -> R.drawable.img_reaction_skull
        }

internal val ReactionEmoji.contentDescriptionRes: Int
    @StringRes
    get() =
        when (this) {
            ReactionEmoji.MEDAL -> R.string.photo_detail_reaction_medal
            ReactionEmoji.HEART -> R.string.photo_detail_reaction_heart
            ReactionEmoji.POOP -> R.string.photo_detail_reaction_poop
            ReactionEmoji.CLAP -> R.string.photo_detail_reaction_clap
            ReactionEmoji.SKULL -> R.string.photo_detail_reaction_skull
        }

@ComposePreview(showBackground = true, widthDp = 390)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoReactionBarPreview() {
    PhotoReactionBar(onEmojiClick = {})
}
