package com.happyhouse.challa.presentation.photodetail.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.IntOffset
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.photodetail.contract.PhotoReactionUiModel
import com.happyhouse.challa.presentation.photodetail.contract.ReactionEmoji
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.roundToInt
import kotlin.random.Random
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

/** 사진 폭 대비 스티커 크기. 피그마 기준 358dp 사진에 약 143dp. */
private const val STICKER_WIDTH_RATIO = 0.4f

/** 자리 안에서 흔들어줄 범위(스티커 크기 대비)와 기울기 범위 */
private const val SLOT_JITTER_RATIO = 0.12f
private const val MAX_TILT_DEGREES = 15f

/** 스티커가 놓이는 자리. 배치 가능 영역 안에서의 비율로, 0이 왼쪽/위, 1이 오른쪽/아래다. */
private data class StickerSlot(
    val horizontalBias: Float,
    val verticalBias: Float,
)

/**
 * 자리 세트. 사진마다 A/B 중 하나를 뽑아 쓴다.
 *
 * 세트 안에서 좌우가 번갈아 나와 스티커가 한쪽에 몰리지 않는다.
 * 어느 세트를 쓸지는 사진 id로 정하므로, 같은 사진은 다시 열어도 같은 세트를 쓴다.
 */
private enum class StickerSlotSet(
    val slots: List<StickerSlot>,
) {
    /** 좌측 상단 → 우측 중앙 → 좌측 하단 */
    A(
        listOf(
            StickerSlot(horizontalBias = 0f, verticalBias = 0f),
            StickerSlot(horizontalBias = 1f, verticalBias = 0.5f),
            StickerSlot(horizontalBias = 0f, verticalBias = 1f),
        ),
    ),

    /** 우측 상단 → 좌측 중앙 → 우측 하단 */
    B(
        listOf(
            StickerSlot(horizontalBias = 1f, verticalBias = 0f),
            StickerSlot(horizontalBias = 0f, verticalBias = 0.5f),
            StickerSlot(horizontalBias = 1f, verticalBias = 1f),
        ),
    ),
}

private data class StickerPlacement(
    val offset: IntOffset,
    val tiltDegrees: Float,
)

/**
 * 사진 위에 반응 스티커를 붙인다.
 *
 * 남긴 순서대로 자리 세트의 1 → 2 → 3번 자리를 채우고, 자리 안에서 위치와 각도를 흔든다.
 * 사진 카드가 clip돼 있어 가장자리로 삐져나온 부분은 잘린다.
 */
@Composable
fun PhotoReactionOverlay(
    photoId: Long,
    reactions: ImmutableList<PhotoReactionUiModel>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val widthPx = constraints.maxWidth
        val heightPx = constraints.maxHeight
        val stickerPx = (widthPx * STICKER_WIDTH_RATIO).roundToInt()
        val stickerSize = with(LocalDensity.current) { stickerPx.toDp() }

        val slotSet = remember(photoId) { StickerSlotSet.entries.random(Random(photoId)) }

        // 개수 상한은 ViewModel이 지킨다. 여기서는 자리 수로 한 번 더 잘라, 상한이 늘어도 크래시하지 않게 한다.
        reactions.take(slotSet.slots.size).forEachIndexed { index, reaction ->
            val slot = slotSet.slots[index]

            key(reaction.id) {
                val placement =
                    remember(reaction.id, slot, widthPx, heightPx, stickerPx) {
                        stickerPlacement(
                            slot = slot,
                            reactionId = reaction.id,
                            placeableWidth = (widthPx - stickerPx).coerceAtLeast(0),
                            placeableHeight = (heightPx - stickerPx).coerceAtLeast(0),
                            stickerPx = stickerPx,
                        )
                    }

                ReactionSticker(
                    modifier =
                        Modifier
                            .size(stickerSize)
                            .offset { placement.offset }
                            .rotate(placement.tiltDegrees),
                    emoji = reaction.emoji,
                )
            }
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
        modifier = modifier,
        painter = painterResource(id = emoji.stickerDrawableRes),
        contentDescription = null,
    )
}

/**
 * 반응 id를 seed로 자리 안에서의 위치와 각도를 정한다.
 * 같은 반응은 스크롤·재진입과 무관하게 항상 같은 모습으로 그려진다.
 */
private fun stickerPlacement(
    slot: StickerSlot,
    reactionId: Long,
    placeableWidth: Int,
    placeableHeight: Int,
    stickerPx: Int,
): StickerPlacement {
    val random = Random(reactionId)
    val jitterPx = stickerPx * SLOT_JITTER_RATIO

    fun jitter(): Float = (random.nextFloat() * 2f - 1f) * jitterPx

    return StickerPlacement(
        offset =
            IntOffset(
                x = (slot.horizontalBias * placeableWidth + jitter()).roundToInt(),
                y = (slot.verticalBias * placeableHeight + jitter()).roundToInt(),
            ),
        tiltDegrees = (random.nextFloat() * 2f - 1f) * MAX_TILT_DEGREES,
    )
}

@ComposePreview(showBackground = true, widthDp = 358, heightDp = 477)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoReactionOverlayPreview() {
    PhotoReactionOverlay(
        modifier = Modifier.fillMaxSize(),
        photoId = 1L,
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
    name = "PhotoReactionOverlay - 반대쪽 자리 세트",
)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoReactionOverlayOtherSlotSetPreview() {
    PhotoReactionOverlay(
        modifier = Modifier.fillMaxSize(),
        photoId = 2L,
        reactions =
            persistentListOf(
                PhotoReactionUiModel(id = 3L, emoji = ReactionEmoji.THINKING),
                PhotoReactionUiModel(id = 4L, emoji = ReactionEmoji.SPARKLES),
                PhotoReactionUiModel(id = 5L, emoji = ReactionEmoji.SKULL),
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
        photoId = 1L,
        reactions = persistentListOf(),
    )
}
