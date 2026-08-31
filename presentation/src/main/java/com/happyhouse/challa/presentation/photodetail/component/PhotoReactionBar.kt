package com.happyhouse.challa.presentation.photodetail.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.clickOnce
import com.happyhouse.challa.presentation.reaction.labelRes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private val ReactionButtonSize = 58.dp
private val ReactionEmojiSize = 32.dp

/** 내가 남겨둔 이모지를 표시하는 테두리 */
private val ReactionSelectedRingWidth = 2.dp

/** 페이지끼리의 간격. 피그마의 버튼 간격과 같은 값이라 넘길 때 리듬이 이어진다. */
private val ReactionBarPageSpacing = 13.dp
private val ReactionBarHorizontalPadding = 24.dp

/**
 * 한 페이지에 노출하는 이모지 수. 스와이프 한 번에 이만큼 넘어간다.
 *
 * [ReactionBarEmojis]의 수가 이 값의 배수라 마지막 페이지도 꽉 찬다. 배수가 아니게 되면 마지막 페이지에서
 * 항목이 양 끝으로 벌어지므로, 그때는 배치를 다시 정해야 한다.
 */
private const val EMOJI_COUNT_PER_PAGE = 5

/**
 * 반응 바에 노출하는 순서.
 *
 * 노출 순서는 화면 규칙이라 [ReactionEmoji]의 선언 순서에 기대지 않는다.
 * 이모지를 추가할 때 [drawableRes] 등의 `when`과 달리 여기는 컴파일 에러로 잡히지 않으니 함께 넣는다.
 */
private val ReactionBarEmojis: ImmutableList<ReactionEmoji> =
    persistentListOf(
        ReactionEmoji.FIRE,
        ReactionEmoji.EYES,
        ReactionEmoji.MEDAL,
        ReactionEmoji.QUESTION,
        ReactionEmoji.THINKING,
        ReactionEmoji.HEART,
        ReactionEmoji.THUMBS_UP,
        ReactionEmoji.SPARKLES,
        ReactionEmoji.POOP,
        ReactionEmoji.SKULL,
    )

/**
 * 이모지를 [EMOJI_COUNT_PER_PAGE]개씩 끊어 좌우로 넘긴다.
 *
 * 자유 스크롤이 아니라 페이지 단위로 딱 떨어져야 해서 pager를 쓴다.
 * 페이지가 화면 폭을 꽉 채워야 좌우 여백이 피그마대로 나오므로, 여백은 modifier가 아닌
 * contentPadding으로 준다.
 *
 * 버튼 간격을 13dp로 고정하지 않고 [Arrangement.SpaceBetween]으로 남는 폭을 나눠 주는 이유:
 * 피그마 기준 폭(390dp)에서는 버튼 5개와 간격 4개가 페이지 폭과 정확히 맞아떨어져 간격이 13dp로 같지만,
 * 360dp 기기에서는 고정 간격이면 폭이 30dp 모자라 마지막 버튼이 잘린다.
 * 나눠 주면 좁은 화면에서는 간격만 줄어들어 5개가 모두 들어온다.
 */
@Composable
fun PhotoReactionBar(
    addedEmojis: ImmutableSet<ReactionEmoji>,
    onEmojiClick: (ReactionEmoji) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pages = remember { ReactionBarEmojis.chunked(EMOJI_COUNT_PER_PAGE) }
    val pagerState = rememberPagerState { pages.size }

    HorizontalPager(
        modifier = modifier.fillMaxWidth(),
        state = pagerState,
        contentPadding = PaddingValues(horizontal = ReactionBarHorizontalPadding),
        pageSpacing = ReactionBarPageSpacing,
    ) { page ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            pages[page].forEach { emoji ->
                ReactionButton(
                    emoji = emoji,
                    isAdded = emoji in addedEmojis,
                    onClick = { onEmojiClick(emoji) },
                )
            }
        }
    }
}

/** @param isAdded 내가 이미 남겨둔 이모지. 링으로 표시하고, 누르면 남기는 대신 취소한다. */
@Composable
private fun ReactionButton(
    emoji: ReactionEmoji,
    isAdded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(ReactionButtonSize)
                .clip(CircleShape)
                .background(ChallaTheme.colors.backgroundLevel2)
                .border(
                    width = if (isAdded) ReactionSelectedRingWidth else 0.dp,
                    color = if (isAdded) ChallaTheme.colors.primaryYellow else Color.Transparent,
                    shape = CircleShape,
                ).clickOnce(
                    role = Role.Button,
                    onClickLabel =
                        stringResource(
                            if (isAdded) {
                                R.string.photo_detail_reaction_remove_description
                            } else {
                                R.string.photo_detail_reaction_add_description
                            },
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

@ComposePreview(showBackground = true, widthDp = 390)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoReactionBarPreview() {
    PhotoReactionBar(
        addedEmojis = persistentSetOf(),
        onEmojiClick = {},
    )
}

@ComposePreview(showBackground = true, widthDp = 390, name = "PhotoReactionBar - 남긴 반응 있음")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoReactionBarWithAddedEmojisPreview() {
    PhotoReactionBar(
        addedEmojis = persistentSetOf(ReactionEmoji.FIRE, ReactionEmoji.MEDAL),
        onEmojiClick = {},
    )
}
