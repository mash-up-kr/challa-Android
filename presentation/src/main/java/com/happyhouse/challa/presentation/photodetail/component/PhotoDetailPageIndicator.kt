package com.happyhouse.challa.presentation.photodetail.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.lerp as lerpDp

private const val FULL_SIZE_WINDOW = 3

private val DotSpacing = 8.dp

private val DotSizeByDistance = listOf(10.dp, 8.dp, 6.dp)
private val SHRINK_STEP_COUNT = DotSizeByDistance.lastIndex
private val MaxDotSize = DotSizeByDistance.first()

/**
 * 인스타그램식 dot 페이지네이션
 */
@Composable
fun PhotoDetailPageIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val pageCount = pagerState.pageCount
    if (pageCount <= 1) return

    val currentColor = ChallaTheme.colors.primaryYellow
    val otherColor = ChallaTheme.colors.labelDisable

    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(MaxDotSize),
    ) {
        val position = pagerState.currentPage + pagerState.currentPageOffsetFraction
        val maxWindowStart = (pageCount - FULL_SIZE_WINDOW).coerceAtLeast(0).toFloat()
        val windowStart = (position - 1f).coerceIn(0f, maxWindowStart)

        val settledWindowStart = windowStart.roundToInt()
        val firstVisiblePage = (settledWindowStart - SHRINK_STEP_COUNT).coerceAtLeast(0)
        val lastVisiblePage =
            (settledWindowStart + FULL_SIZE_WINDOW - 1 + SHRINK_STEP_COUNT).coerceAtMost(pageCount - 1)
        val hasHiddenBefore = firstVisiblePage > 0
        val hasHiddenAfter = lastVisiblePage < pageCount - 1

        val spacing = DotSpacing.toPx()
        val diameters =
            (firstVisiblePage..lastVisiblePage).map { page ->
                dotSizeOf(
                    page = page,
                    windowStart = windowStart,
                    hasHiddenBefore = hasHiddenBefore,
                    hasHiddenAfter = hasHiddenAfter,
                ).toPx()
            }

        var left = (size.width - diameters.sum() - spacing * (diameters.size - 1)) / 2f
        diameters.forEachIndexed { index, diameter ->
            val radius = diameter / 2f
            drawCircle(
                color =
                    lerpColor(
                        start = otherColor,
                        stop = currentColor,
                        fraction = proximityOf(page = firstVisiblePage + index, position = position),
                    ),
                radius = radius,
                center = Offset(x = left + radius, y = size.height / 2f),
            )
            left += diameter + spacing
        }
    }
}

/**
 * 창을 벗어난 칸 수로 dot 크기를 정한다.
 */
private fun dotSizeOf(
    page: Int,
    windowStart: Float,
    hasHiddenBefore: Boolean,
    hasHiddenAfter: Boolean,
): Dp {
    val windowEnd = windowStart + FULL_SIZE_WINDOW - 1
    val distance =
        when {
            page < windowStart -> if (hasHiddenBefore) windowStart - page else 0f
            page > windowEnd -> if (hasHiddenAfter) page - windowEnd else 0f
            else -> 0f
        }.coerceIn(0f, SHRINK_STEP_COUNT.toFloat())

    val step = distance.toInt()
    return lerpDp(
        start = DotSizeByDistance[step],
        stop = DotSizeByDistance[(step + 1).coerceAtMost(SHRINK_STEP_COUNT)],
        fraction = distance - step,
    )
}

private fun proximityOf(
    page: Int,
    position: Float,
): Float = (1f - abs(page - position)).coerceIn(0f, 1f)

@ComposePreview(showBackground = true, backgroundColor = 0xFF111111, widthDp = 390, name = "사진 24장 - 첫 페이지")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPageIndicatorFirstPagePreview() {
    PhotoDetailPageIndicator(pagerState = rememberPagerState(initialPage = 0) { 24 })
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF111111, widthDp = 390, name = "사진 24장 - 중간 페이지")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPageIndicatorMiddlePagePreview() {
    PhotoDetailPageIndicator(pagerState = rememberPagerState(initialPage = 10) { 24 })
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF111111, widthDp = 390, name = "사진 24장 - 마지막 페이지")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPageIndicatorLastPagePreview() {
    PhotoDetailPageIndicator(pagerState = rememberPagerState(initialPage = 23) { 24 })
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF111111, widthDp = 390, name = "사진 4장")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPageIndicatorFourPhotosPreview() {
    PhotoDetailPageIndicator(pagerState = rememberPagerState(initialPage = 1) { 4 })
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF111111, widthDp = 390, name = "사진 2장")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPageIndicatorTwoPhotosPreview() {
    PhotoDetailPageIndicator(pagerState = rememberPagerState(initialPage = 0) { 2 })
}
