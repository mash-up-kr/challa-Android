package com.happyhouse.challa.presentation.photodetail.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

private val ReactionButtonSize = 58.dp
private val ReactionEmojiSize = 32.dp
private val ReactionBarSpacing = 13.dp
private val ReactionBarHorizontalPadding = 24.dp

/** 한 페이지에 노출하는 이모지 수. 스와이프 한 번에 이만큼 넘어간다. */
private const val EMOJI_COUNT_PER_PAGE = 5

/**
 * 이모지를 [EMOJI_COUNT_PER_PAGE]개씩 끊어 좌우로 넘긴다.
 *
 * 자유 스크롤이 아니라 페이지 단위로 딱 떨어져야 해서 pager를 쓴다.
 * 페이지 폭이 화면에서 좌우 여백을 뺀 만큼이어야 5개가 정확히 들어차므로,
 * 여백은 modifier가 아닌 contentPadding으로 준다.
 */
@Composable
fun PhotoReactionBar(
    onEmojiClick: (ReactionEmoji) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pages = remember { ReactionEmoji.entries.chunked(EMOJI_COUNT_PER_PAGE) }
    val pagerState = rememberPagerState { pages.size }

    HorizontalPager(
        modifier = modifier.fillMaxWidth(),
        state = pagerState,
        contentPadding = PaddingValues(horizontal = ReactionBarHorizontalPadding),
        pageSpacing = ReactionBarSpacing,
    ) { page ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ReactionBarSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            pages[page].forEach { emoji ->
                ReactionButton(
                    emoji = emoji,
                    onClick = { onEmojiClick(emoji) },
                )
            }
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
                    onClickLabel =
                        stringResource(
                            R.string.photo_detail_reaction_add_description,
                            stringResource(emoji.labelRes),
                        ),
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

/** 하단 반응 바에 쓰는 테두리 없는 이모지 */
internal val ReactionEmoji.drawableRes: Int
    @DrawableRes
    get() =
        when (this) {
            ReactionEmoji.FIRE -> R.drawable.img_reaction_fire
            ReactionEmoji.EYES -> R.drawable.img_reaction_eyes
            ReactionEmoji.MEDAL -> R.drawable.img_reaction_medal
            ReactionEmoji.QUESTION -> R.drawable.img_reaction_question
            ReactionEmoji.THINKING -> R.drawable.img_reaction_thinking
            ReactionEmoji.HEART -> R.drawable.img_reaction_heart
            ReactionEmoji.THUMBS_UP -> R.drawable.img_reaction_thumbs_up
            ReactionEmoji.SPARKLES -> R.drawable.img_reaction_sparkles
            ReactionEmoji.POOP -> R.drawable.img_reaction_poop
            ReactionEmoji.SKULL -> R.drawable.img_reaction_skull
        }

/** 사진 위에 붙는 스티커용. 흰 테두리가 이미지에 포함돼 있어 별도 배경을 그리지 않는다. */
internal val ReactionEmoji.stickerDrawableRes: Int
    @DrawableRes
    get() =
        when (this) {
            ReactionEmoji.FIRE -> R.drawable.img_reaction_sticker_fire
            ReactionEmoji.EYES -> R.drawable.img_reaction_sticker_eyes
            ReactionEmoji.MEDAL -> R.drawable.img_reaction_sticker_medal
            ReactionEmoji.QUESTION -> R.drawable.img_reaction_sticker_question
            ReactionEmoji.THINKING -> R.drawable.img_reaction_sticker_thinking
            ReactionEmoji.HEART -> R.drawable.img_reaction_sticker_heart
            ReactionEmoji.THUMBS_UP -> R.drawable.img_reaction_sticker_thumbs_up
            ReactionEmoji.SPARKLES -> R.drawable.img_reaction_sticker_sparkles
            ReactionEmoji.POOP -> R.drawable.img_reaction_sticker_poop
            ReactionEmoji.SKULL -> R.drawable.img_reaction_sticker_skull
        }

internal val ReactionEmoji.labelRes: Int
    @StringRes
    get() =
        when (this) {
            ReactionEmoji.FIRE -> R.string.photo_detail_reaction_fire
            ReactionEmoji.EYES -> R.string.photo_detail_reaction_eyes
            ReactionEmoji.MEDAL -> R.string.photo_detail_reaction_medal
            ReactionEmoji.QUESTION -> R.string.photo_detail_reaction_question
            ReactionEmoji.THINKING -> R.string.photo_detail_reaction_thinking
            ReactionEmoji.HEART -> R.string.photo_detail_reaction_heart
            ReactionEmoji.THUMBS_UP -> R.string.photo_detail_reaction_thumbs_up
            ReactionEmoji.SPARKLES -> R.string.photo_detail_reaction_sparkles
            ReactionEmoji.POOP -> R.string.photo_detail_reaction_poop
            ReactionEmoji.SKULL -> R.string.photo_detail_reaction_skull
        }

@ComposePreview(showBackground = true, widthDp = 390)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoReactionBarPreview() {
    PhotoReactionBar(onEmojiClick = {})
}
