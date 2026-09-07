package com.happyhouse.challa.presentation.photodetail.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.clickOnce
import com.happyhouse.challa.presentation.reaction.labelRes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private val ReactionButtonSize = 58.dp
private val ReactionEmojiSize = 32.dp

private val ReactionBarItemSpacing = 13.dp
private val ReactionBarHorizontalPadding = 24.dp

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
 * 페이지 단위로 끊지 않는다. 피그마 기준 폭(390dp)에서 다섯 번째 다음 이모지가 오른쪽 끝에 걸쳐,
 * 옆으로 더 있다는 것이 보이는 것을 노린 배치다.
 *
 * 스크롤 영역이 화면 끝까지 닿아야 걸친 이모지가 잘리지 않고 그려지므로,
 * 좌우 여백은 modifier가 아닌 contentPadding으로 준다.
 */
@Composable
fun PhotoReactionBar(
    onEmojiClick: (ReactionEmoji) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = ReactionBarHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(ReactionBarItemSpacing),
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
