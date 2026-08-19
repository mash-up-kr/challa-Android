package com.happyhouse.challa.presentation.photodetail.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.photodetail.contract.PhotoReactionUiModel
import com.happyhouse.challa.presentation.photodetail.contract.ReactionEmoji
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

// TODO: 배치 규칙(Case A/B 3자리) 적용은 후속 커밋에서. (이슈 #110)
private val ReactionStickerSize = 130.dp

/**
 * 스티커 중심을 사진 중심에서 얼마나 떨어뜨릴지의 범위(사진 크기 대비 비율).
 * 정중앙을 피해서 배치해야 해서 최소 거리를 둔다.
 */
private const val MIN_CENTER_DISTANCE = 0.3
private const val MAX_CENTER_DISTANCE = 0.5

@Composable
fun PhotoReactionOverlay(
    reactions: ImmutableList<PhotoReactionUiModel>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val widthPx = constraints.maxWidth
        val heightPx = constraints.maxHeight
        val stickerPx = with(LocalDensity.current) { ReactionStickerSize.roundToPx() }

        reactions.forEach { reaction ->
            val offset =
                remember(reaction.id, widthPx, heightPx, stickerPx) {
                    reactionStickerOffset(
                        reactionId = reaction.id,
                        widthPx = widthPx,
                        heightPx = heightPx,
                        stickerPx = stickerPx,
                    )
                }

            ReactionSticker(
                modifier = Modifier.offset { offset },
                emoji = reaction.emoji,
            )
        }
    }
}

/** 스티커 에셋에 흰 테두리가 포함돼 있어 배경을 따로 그리지 않는다. */
@Composable
private fun ReactionSticker(
    emoji: ReactionEmoji,
    modifier: Modifier = Modifier,
) {
    Image(
        modifier = modifier.size(ReactionStickerSize),
        painter = painterResource(id = emoji.stickerDrawableRes),
        contentDescription = null,
    )
}

/**
 * 반응 id를 seed로 스티커 좌표를 정한다. 같은 반응은 스크롤·재진입과 무관하게 항상 같은 자리에 그려진다.
 *
 * 스티커가 사진 밖으로 나가지 않도록 배치 범위는 사진 크기에서 스티커 크기를 뺀 영역으로 잡는다.
 */
private fun reactionStickerOffset(
    reactionId: Long,
    widthPx: Int,
    heightPx: Int,
    stickerPx: Int,
): IntOffset {
    val random = Random(reactionId)
    val angle = random.nextDouble(0.0, 2 * PI)
    val distance = random.nextDouble(MIN_CENTER_DISTANCE, MAX_CENTER_DISTANCE)

    val placeableWidth = (widthPx - stickerPx).coerceAtLeast(0)
    val placeableHeight = (heightPx - stickerPx).coerceAtLeast(0)

    return IntOffset(
        x = (placeableWidth * (0.5 + cos(angle) * distance)).roundToInt().coerceIn(0, placeableWidth),
        y = (placeableHeight * (0.5 + sin(angle) * distance)).roundToInt().coerceIn(0, placeableHeight),
    )
}

@ComposePreview(showBackground = true, widthDp = 358, heightDp = 477)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoReactionOverlayPreview() {
    PhotoReactionOverlay(
        modifier = Modifier.fillMaxSize(),
        reactions =
            persistentListOf(
                PhotoReactionUiModel(id = 0L, emoji = ReactionEmoji.MEDAL),
                PhotoReactionUiModel(id = 1L, emoji = ReactionEmoji.HEART),
                PhotoReactionUiModel(id = 2L, emoji = ReactionEmoji.FIRE),
            ),
    )
}

@ComposePreview(
    showBackground = true,
    widthDp = 358,
    heightDp = 477,
    name = "PhotoReactionOverlay - 반응 없음",
)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoReactionOverlayEmptyPreview() {
    PhotoReactionOverlay(
        modifier = Modifier.fillMaxSize(),
        reactions = persistentListOf(),
    )
}
