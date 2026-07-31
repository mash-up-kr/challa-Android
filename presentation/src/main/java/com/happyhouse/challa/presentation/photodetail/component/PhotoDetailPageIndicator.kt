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
import kotlin.math.ceil
import kotlin.math.floor
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.lerp as lerpDp

// 현재 페이지 + 좌우 1칸
private const val FULL_SIZE_WINDOW = 3

private val DotSpacing = 8.dp

// 창 밖으로 벗어난 칸 수별 dot 지름. 마지막 0.dp가 있어야 dot이 0에서부터 자란다(없으면 행 폭이 튄다).
private val DotSizeByDistance = listOf(10.dp, 8.dp, 6.dp, 0.dp)

private val ShrinkStepCount = DotSizeByDistance.lastIndex
private val VisibleSideDotCount = ShrinkStepCount - 1
private val MaxDotSize = DotSizeByDistance.first()

/** 인스타그램식 dot 페이지네이션 */
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

        // dot이 한 화면에 다 담기는 개수면 축소하지 않는다.
        val shrinksAtEdge = pageCount > FULL_SIZE_WINDOW + VisibleSideDotCount

        // floor/ceil로 지름 0인 칸까지 포함해야 dot이 들고 날 때도 행 폭이 연속적으로 변한다.
        val firstVisiblePage = (floor(windowStart).toInt() - ShrinkStepCount).coerceAtLeast(0)
        val lastVisiblePage =
            (ceil(windowStart).toInt() + FULL_SIZE_WINDOW - 1 + ShrinkStepCount)
                .coerceAtMost(pageCount - 1)

        val spacing = DotSpacing.toPx()

        // 매 프레임 도는 경로라 리스트 없이 두 번 순회한다(폭 합산 → 그리기).
        var rowWidth = 0f
        var previousPresence = 0f
        for (page in firstVisiblePage..lastVisiblePage) {
            val distance = distanceFromWindow(page = page, windowStart = windowStart, shrinks = shrinksAtEdge)
            val presence = presenceOf(distance)
            // 사라지는 dot은 간격도 같이 줄여야 행 폭이 연속적으로 변한다.
            if (page > firstVisiblePage) rowWidth += spacing * minOf(previousPresence, presence)
            rowWidth += dotSizeOf(distance).toPx()
            previousPresence = presence
        }

        var left = (size.width - rowWidth) / 2f
        previousPresence = 0f
        for (page in firstVisiblePage..lastVisiblePage) {
            val distance = distanceFromWindow(page = page, windowStart = windowStart, shrinks = shrinksAtEdge)
            val presence = presenceOf(distance)
            if (page > firstVisiblePage) left += spacing * minOf(previousPresence, presence)

            val radius = dotSizeOf(distance).toPx() / 2f
            drawCircle(
                color =
                    lerpColor(
                        start = otherColor,
                        stop = currentColor,
                        fraction = proximityOf(page = page, position = position),
                    ),
                radius = radius,
                center = Offset(x = left + radius, y = size.height / 2f),
            )

            left += radius * 2f
            previousPresence = presence
        }
    }
}

/** 창([FULL_SIZE_WINDOW]칸)에서 몇 칸 벗어났는지. 스와이프 중 끊기지 않도록 실수로 계산한다. */
private fun distanceFromWindow(
    page: Int,
    windowStart: Float,
    shrinks: Boolean,
): Float {
    if (!shrinks) return 0f

    val windowEnd = windowStart + FULL_SIZE_WINDOW - 1
    return when {
        page < windowStart -> windowStart - page
        page > windowEnd -> page - windowEnd
        else -> 0f
    }.coerceIn(0f, ShrinkStepCount.toFloat())
}

private fun dotSizeOf(distance: Float): Dp {
    val step = distance.toInt()
    return lerpDp(
        start = DotSizeByDistance[step],
        stop = DotSizeByDistance[(step + 1).coerceAtMost(ShrinkStepCount)],
        fraction = distance - step,
    )
}

/** dot이 얼마나 남아 있는지(1 = 온전히 보임, 0 = 사라짐). */
private fun presenceOf(distance: Float): Float = (ShrinkStepCount - distance).coerceIn(0f, 1f)

private fun proximityOf(
    page: Int,
    position: Float,
): Float = (1f - abs(page - position)).coerceIn(0f, 1f)

@ComposePreview(showBackground = true, widthDp = 390, name = "사진 24장 - 첫 페이지")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPageIndicatorFirstPagePreview() {
    PhotoDetailPageIndicator(pagerState = rememberPagerState(initialPage = 0) { 24 })
}

@ComposePreview(showBackground = true, widthDp = 390, name = "사진 24장 - 중간 페이지")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPageIndicatorMiddlePagePreview() {
    PhotoDetailPageIndicator(pagerState = rememberPagerState(initialPage = 10) { 24 })
}

@ComposePreview(showBackground = true, widthDp = 390, name = "사진 24장 - 마지막 페이지")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPageIndicatorLastPagePreview() {
    PhotoDetailPageIndicator(pagerState = rememberPagerState(initialPage = 23) { 24 })
}

@ComposePreview(showBackground = true, widthDp = 390, name = "사진 4장")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPageIndicatorFourPhotosPreview() {
    PhotoDetailPageIndicator(pagerState = rememberPagerState(initialPage = 1) { 4 })
}

@ComposePreview(showBackground = true, widthDp = 390, name = "사진 2장")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPageIndicatorTwoPhotosPreview() {
    PhotoDetailPageIndicator(pagerState = rememberPagerState(initialPage = 0) { 2 })
}
