package com.happyhouse.challa.presentation.photodetail.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.clickOnce
import com.happyhouse.challa.presentation.reaction.labelRes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.absoluteValue
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private val ReactionButtonSize = 58.dp
private val ReactionEmojiSize = 32.dp

/** 피그마 기준 간격. 화면 폭에 맞춰 이 값에 가장 가깝게 잡는다. */
private val ReactionBarBaseItemSpacing = 13.dp

/** 좁은 화면에서 버튼끼리 붙어 보이지 않게 하는 하한 */
private val ReactionBarMinItemSpacing = 8.dp

private val ReactionBarHorizontalPadding = 24.dp

/** 다음 이모지를 버튼 폭의 이만큼 걸쳐 보여, 옆으로 더 있다는 것을 알린다. */
private const val REACTION_BAR_PEEK_RATIO = 0.5f

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
 * 이모지를 좌우로 스크롤해 고른다.
 *
 * 페이지 단위로 끊지 않는다. 다음 이모지가 오른쪽 끝에 반쯤 걸쳐,
 * 옆으로 더 있다는 것이 보이는 것을 인식하게끔 한다.
 *
 * 스크롤 영역이 화면 끝까지 닿아야 걸친 이모지가 잘리지 않고 그려지므로,
 * 좌우 여백은 modifier가 아닌 contentPadding으로 준다.
 */
@Composable
fun PhotoReactionBar(
    onEmojiClick: (ReactionEmoji) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        // 왼쪽 여백을 뺀, 이모지가 실제로 놓이는 폭
        val contentWidth = maxWidth - ReactionBarHorizontalPadding
        val itemSpacing = remember(contentWidth) { reactionBarItemSpacing(contentWidth) }

        LazyRow(
            contentPadding = PaddingValues(horizontal = ReactionBarHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            items(
                items = ReactionBarEmojis,
                key = { emoji -> emoji.name },
            ) { emoji ->
                ReactionButton(
                    emoji = emoji,
                    onClick = { onEmojiClick(emoji) },
                )
            }
        }
    }
}

/**
 * 걸쳐 보이는 폭이 항상 버튼의 [REACTION_BAR_PEEK_RATIO]가 되도록 항목 간격을 정한다.
 *
 * 꽉 차게 보일 개수는 간격이 [ReactionBarBaseItemSpacing]에 가장 가까워지는 값으로 고른다.
 * 어느 개수로도 [ReactionBarMinItemSpacing]을 못 지키는 좁은 화면에서는 하한을 쓰고 걸침을 포기한다.
 */
private fun reactionBarItemSpacing(contentWidth: Dp): Dp {
    val peekWidth = ReactionButtonSize * REACTION_BAR_PEEK_RATIO

    return (1 until ReactionBarEmojis.size)
        .map { fullCount -> (contentWidth - ReactionButtonSize * fullCount - peekWidth) / fullCount }
        .filter { spacing -> spacing >= ReactionBarMinItemSpacing }
        .minByOrNull { spacing -> (spacing - ReactionBarBaseItemSpacing).value.absoluteValue }
        ?: ReactionBarMinItemSpacing
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

@ComposePreview(showBackground = true, widthDp = 390)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoReactionBarPreview() {
    PhotoReactionBar(onEmojiClick = {})
}

@ComposePreview(showBackground = true, widthDp = 360, name = "PhotoReactionBar - 좁은 화면")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoReactionBarNarrowPreview() {
    PhotoReactionBar(onEmojiClick = {})
}
