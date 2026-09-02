package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.foundation.motion.MotionTokens
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.gallery.contract.GalleryPhotoUiModel
import com.happyhouse.challa.presentation.gallery.previewGalleryPhotos
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

/** 필름이 나오는 입구 */
private val DispenserTopPadding = 12.dp
private val DispenserHeight = 38.dp
private val DispenserHorizontalPadding = 34.dp
private val DispenserBorderWidth = 4.dp
private val DispenserSlotHeight = 6.dp
private val DispenserSlotHorizontalPadding = 28.dp
private val DispenserShape = RoundedCornerShape(percent = 60)

private val FilmWindowTopPadding = DispenserTopPadding + (DispenserHeight + DispenserSlotHeight) / 2

/** 필름 한 칸 */
private const val FILM_CELL_ASPECT_RATIO = 4f / 3f
private const val FILM_STRIP_WIDTH_RATIO = 0.58f

/** 세로 사진을 눕혀 가로 칸을 채운다. */
private const val FILM_PHOTO_ROTATION_DEGREES = 90f
private val FilmSprocketAreaWidth = 18.dp
private val FilmCellSpacing = 10.dp
private val FilmSprocketSize = 6.dp
private val FilmSprocketCornerRadius = 1.dp
private val FilmSprocketPitch = 24.dp
private const val FILM_SPROCKET_ALPHA = 0.4f

private const val FILM_PEEK_CELL_RATIO = 1.3f

private val PullHintGap = 16.dp

private val PullHintBounceDistance = 10.dp
private const val PULL_HINT_BOUNCE_MS = 900

/** 넘기면 나머지는 자동으로 흘러내린다. */
private val PullTriggerDistance = 56.dp

/**
 * 필름이 흘러내리는 시간. 칸 수에 비례하되 너무 짧거나 지루하지 않게 자른다.
 *
 * 상한에 걸리는 큰 방(72칸)은 칸당 시간이 저절로 짧아져, 작은 방과 큰 방의 속도가 알아서 갈린다.
 */
private const val ROLL_BASE_MS = 1_200
private const val ROLL_MS_PER_PHOTO = 130
private const val ROLL_MIN_MS = 2_000
private const val ROLL_MAX_MS = 8_000

private const val REVEAL_STAGGER_MS = 45L

private const val REVEAL_SCROLL_MS = 160

/** 등장이 끝난 뒤 마지막 줄을 맞추는 시도 횟수와, 여백이 늘어나길 기다리는 간격 */
private const val REVEAL_SETTLE_ATTEMPTS = 6
private const val REVEAL_SETTLE_DELAY_MS = 80L

private const val ROLL_TO_REVEAL_DELAY_MS = 200L

/** 화면 안에서만 쓰이므로 State에 두지 않는다. */
private enum class PrintAnimationPhase {
    PULL_WAITING,
    ROLLING,
    REVEALING,
}

/**
 * 인화 완료 연출. 필름을 당겨 뽑으면 사진이 그리드에 하나씩 나타난다.
 *
 * @param gridState 남는 그리드와 같은 것을 넘기면 스크롤 위치가 이어진다.
 * @param extraBottomPadding 사진이 나타나는 동안에도 하단 바에 마지막 줄이 가리지 않도록 더하는 여백
 * @param onFilmStageChange 필름이 나오는 동안 true. 배출구가 프로필 바 자리를 쓴다.
 */
@Composable
fun GalleryPrintAnimation(
    photos: ImmutableList<GalleryPhotoUiModel>,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    extraBottomPadding: Dp = 0.dp,
    onFilmStageChange: (Boolean) -> Unit = {},
) {
    var phase by remember { mutableStateOf(PrintAnimationPhase.PULL_WAITING) }

    val showsFilm =
        phase == PrintAnimationPhase.PULL_WAITING || phase == PrintAnimationPhase.ROLLING
    LaunchedEffect(showsFilm) { onFilmStageChange(showsFilm) }

    // 연출 도중 화면을 벗어나면 프로필 바가 비어 있는 채로 남는다.
    DisposableEffect(Unit) {
        onDispose { onFilmStageChange(false) }
    }

    if (photos.isEmpty()) {
        LaunchedEffect(Unit) { onComplete() }
        return
    }

    when (phase) {
        PrintAnimationPhase.PULL_WAITING,
        PrintAnimationPhase.ROLLING,
        ->
            FilmDispensingStage(
                modifier = modifier,
                photos = photos,
                rolls = phase == PrintAnimationPhase.ROLLING,
                onPulled = { phase = PrintAnimationPhase.ROLLING },
                onRolled = { phase = PrintAnimationPhase.REVEALING },
            )

        PrintAnimationPhase.REVEALING ->
            PhotoRevealStage(
                modifier = modifier,
                photos = photos,
                gridState = gridState,
                extraBottomPadding = extraBottomPadding,
                onComplete = onComplete,
            )
    }
}

/** @param rolls true면 당김이 끝나 자동으로 흘러내린다. */
@Composable
private fun FilmDispensingStage(
    photos: ImmutableList<GalleryPhotoUiModel>,
    rolls: Boolean,
    onPulled: () -> Unit,
    onRolled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 입구를 먼저 그려 필름이 그 앞을 지나게 한다.
        FilmDispenser(modifier = Modifier.align(Alignment.TopCenter).dispenserPlacement())

        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = FilmWindowTopPadding)
                    .clipToBounds(),
        ) {
            val density = LocalDensity.current
            val stripWidth = maxWidth * FILM_STRIP_WIDTH_RATIO
            val cellWidth = stripWidth - FilmSprocketAreaWidth * 2
            val cellHeight = cellWidth / FILM_CELL_ASPECT_RATIO

            val cellPitch = cellHeight + FilmCellSpacing
            val cellPitchPx = with(density) { cellPitch.toPx() }
            val viewportPx = with(density) { maxHeight.toPx() }

            // 맨 아래 칸(1번 사진)만 나와 있는 상태에서 필름 전체가 화면 아래로 빠져나갈 때까지.
            val filmPeek = cellPitch * FILM_PEEK_CELL_RATIO
            val filmTopStartPx = with(density) { filmPeek.toPx() } - cellPitchPx * photos.size
            val travelPx = viewportPx - filmTopStartPx
            val pullTriggerPx = with(density) { PullTriggerDistance.toPx() }
            val bounceDistancePx = with(density) { PullHintBounceDistance.toPx() }

            val progress = remember { Animatable(0f) }
            val bounce = pullHintBounce(enabled = !rolls)
            val scope = rememberCoroutineScope()

            // 걸치는 칸이 바뀔 때만 다시 구성된다. 필름이 흐르는 동안 매 프레임 도는 것을 막는다.
            val visibleCells by remember(photos.size, cellPitchPx, viewportPx, filmTopStartPx, travelPx) {
                derivedStateOf {
                    val filmTopPx = filmTopStartPx + travelPx * progress.value
                    val first = (floor(-filmTopPx / cellPitchPx).toInt() - 1).coerceAtLeast(0)
                    val last =
                        (ceil((viewportPx - filmTopPx) / cellPitchPx).toInt() + 1)
                            .coerceAtMost(photos.lastIndex)
                    first..last
                }
            }

            LaunchedEffect(rolls) {
                if (!rolls) return@LaunchedEffect

                val durationMillis =
                    (ROLL_BASE_MS + photos.size * ROLL_MS_PER_PHOTO)
                        .coerceIn(ROLL_MIN_MS, ROLL_MAX_MS)
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = durationMillis, easing = MotionTokens.EaseInOut),
                )
                onRolled()
            }

            // 한 덩어리로 그리면 방이 클수록 레이어가 기기 한계를 넘어 통째로 사라진다.
            // 칸마다 그리되, 72칸 방의 사진을 전부 물고 있지 않도록 화면에 걸치는 것만 그린다.
            // 아래로 흘러내리며 1번 사진부터 보이도록 뒤에서부터 쌓는다.
            visibleCells.forEach { index ->
                val photo = photos[photos.lastIndex - index]

                FilmCell(
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            // 배치 단계에서 읽어 매 프레임 재구성되지 않게 한다.
                            .offset {
                                val filmTopPx = filmTopStartPx + travelPx * progress.value
                                val bouncePx = bounceDistancePx * bounce.value
                                IntOffset(x = 0, y = (filmTopPx + cellPitchPx * index + bouncePx).roundToInt())
                            },
                    imageUrl = photo.imageUrl,
                    stripWidth = stripWidth,
                    cellWidth = cellWidth,
                    cellHeight = cellHeight,
                    cellPitch = cellPitch,
                )
            }

            if (!rolls) {
                var pulledPx by remember { mutableFloatStateOf(0f) }

                Box(
                    modifier =
                        Modifier
                            .matchParentSize()
                            .draggable(
                                orientation = Orientation.Vertical,
                                state =
                                    rememberDraggableState { delta ->
                                        if (delta <= 0f) return@rememberDraggableState

                                        // snapTo가 코루틴으로 밀리는 사이 다음 델타가 오면
                                        // progress.value가 아직 이전 값이라 그만큼 유실된다.
                                        pulledPx += delta
                                        val pulled = pulledPx
                                        scope.launch { progress.snapTo(pulled / travelPx) }
                                        if (pulled >= pullTriggerPx) onPulled()
                                    },
                            ),
                )

                GalleryTooltip(
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = filmPeek + PullHintGap),
                    text = stringResource(R.string.gallery_print_animation_pull_hint),
                )
            }
        }
    }
}

/** @param cellPitch 칸 하나가 차지하는 높이. [cellHeight]와의 차이가 다음 칸과의 여백이 된다. */
@Composable
private fun FilmCell(
    imageUrl: String,
    stripWidth: Dp,
    cellWidth: Dp,
    cellHeight: Dp,
    cellPitch: Dp,
    modifier: Modifier = Modifier,
) {
    val sprocketColor = ChallaTheme.colors.staticWhite.copy(alpha = FILM_SPROCKET_ALPHA)

    Box(
        modifier =
            modifier
                .width(stripWidth)
                .height(cellPitch)
                .background(ChallaTheme.colors.staticBlack)
                .drawBehind { drawFilmSprockets(color = sprocketColor) },
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .size(width = cellWidth, height = cellHeight)
                    .clipToBounds(),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                modifier =
                    Modifier
                        // 회전 전 크기라 가로세로를 바꿔 잡는다. 90도 돌리면 칸에 맞는다.
                        .requiredSize(width = cellHeight, height = cellWidth)
                        .graphicsLayer { rotationZ = FILM_PHOTO_ROTATION_DEGREES },
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun PhotoRevealStage(
    photos: ImmutableList<GalleryPhotoUiModel>,
    gridState: LazyGridState,
    extraBottomPadding: Dp,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var revealedCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(photos.size) {
        delay(ROLL_TO_REVEAL_DELAY_MS)

        // 한 화면을 넘으면 아래쪽 사진이 화면 밖에서 나타난다. 등장을 따라 그리드를 내리되,
        // 등장 간격이 밀리지 않도록 따로 돌린다.
        val followJob =
            launch {
                snapshotFlow { revealedCount }
                    .collect { count -> gridState.followReveal(revealedIndex = count - 1) }
            }

        while (revealedCount < photos.size) {
            revealedCount++
            delay(REVEAL_STAGGER_MS)
        }

        followJob.cancelAndJoin()

        // 하단 바는 등장 단계에서야 올라오고, 그리드 아래 여백도 그때 늘어난다.
        // 여백이 자리잡기 전에 멈추면 마지막 줄이 바에 걸린 채로 남으므로 몇 번 더 맞춘다.
        repeat(REVEAL_SETTLE_ATTEMPTS) {
            if (!gridState.followReveal(photos.lastIndex)) delay(REVEAL_SETTLE_DELAY_MS)
        }

        onComplete()
    }

    GalleryPhotoGrid(
        modifier = modifier,
        photos = photos,
        state = gridState,
        extraBottomPadding = extraBottomPadding,
        reveal = GalleryPhotoReveal(revealedCount = revealedCount),
        onPhotoClick = {},
        // 다음 페이지는 연출 전에 미리 받아둔다.
        onLoadMore = {},
    )
}

/**
 * 방금 나타난 칸이 다 보이도록 그리드를 내린다.
 *
 * `visibleItemsInfo`에는 아래가 잘린 칸도 들어 있어, 목록에 있다는 것만으로는 다 보인다고 볼 수 없다.
 * 마지막 줄이 하단 바에 걸친 채로 끝나지 않도록 잘린 만큼을 재서 그만큼만 내린다.
 *
 * @return 실제로 내렸으면 true. 더 내려갈 곳이 없으면 false.
 */
private suspend fun LazyGridState.followReveal(revealedIndex: Int): Boolean {
    if (revealedIndex < 0) return false

    val info = layoutInfo
    val lastVisible = info.visibleItemsInfo.lastOrNull() ?: return false
    val revealed = info.visibleItemsInfo.firstOrNull { item -> item.index == revealedIndex }

    val scrollBy =
        if (revealed == null) {
            // 아직 화면 아래에 있어 배치되지 않았다. 한 줄만큼 내리고 다음 칸에서 이어서 맞춘다.
            lastVisible.size.height + info.mainAxisItemSpacing
        } else {
            val viewportEnd = info.viewportEndOffset - info.afterContentPadding
            revealed.offset.y + revealed.size.height - viewportEnd
        }

    if (scrollBy <= 0) return false

    animateScrollBy(
        value = scrollBy.toFloat(),
        animationSpec = tween(durationMillis = REVEAL_SCROLL_MS),
    )
    return true
}

/** 당기고 싶게 유도하는 위아래 반복 모션. 0f~1f 비율로 돌려준다. */
@Composable
private fun pullHintBounce(enabled: Boolean): State<Float> {
    val transition = rememberInfiniteTransition(label = "GalleryPrintPullHint")
    val bounce =
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = PULL_HINT_BOUNCE_MS, easing = MotionTokens.EaseOut),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "GalleryPrintPullHintOffset",
        )

    // 트랜지션은 살려두고 진폭만 0으로 둔다.
    return remember(enabled, bounce) {
        derivedStateOf { if (enabled) bounce.value else 0f }
    }
}

private fun Modifier.dispenserPlacement(): Modifier =
    this
        .padding(
            top = DispenserTopPadding,
            start = DispenserHorizontalPadding,
            end = DispenserHorizontalPadding,
        ).fillMaxWidth()
        .height(DispenserHeight)

/** 필름보다 먼저 그린다. 필름이 앞을 지나가므로 아래쪽 테두리는 가려진다. */
@Composable
private fun FilmDispenser(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .clip(DispenserShape)
                .background(ChallaTheme.colors.staticBlack)
                .border(
                    width = DispenserBorderWidth,
                    color = ChallaTheme.colors.primary,
                    shape = DispenserShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .padding(horizontal = DispenserSlotHorizontalPadding)
                    .fillMaxWidth()
                    .height(DispenserSlotHeight)
                    .clip(DispenserShape)
                    .background(ChallaTheme.colors.backgroundLevel4),
        )
    }
}

/** 필름 양쪽 가장자리의 구멍 */
private fun DrawScope.drawFilmSprockets(color: Color) {
    val sprocketSizePx = FilmSprocketSize.toPx()
    val pitchPx = FilmSprocketPitch.toPx()
    val insetPx = (FilmSprocketAreaWidth.toPx() - sprocketSizePx) / 2
    val cornerRadius = CornerRadius(FilmSprocketCornerRadius.toPx())
    val sprocketSize = Size(sprocketSizePx, sprocketSizePx)

    var y = pitchPx / 2
    while (y < size.height) {
        drawRoundRect(
            color = color,
            topLeft = Offset(insetPx, y),
            size = sprocketSize,
            cornerRadius = cornerRadius,
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width - insetPx - sprocketSizePx, y),
            size = sprocketSize,
            cornerRadius = cornerRadius,
        )
        y += pitchPx
    }
}

@ComposePreview(showBackground = true, widthDp = 390, heightDp = 844, name = "인화 연출 - 당김 대기")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryPrintAnimationPullWaitingPreview() {
    GalleryPrintAnimation(
        photos = previewGalleryPhotos(count = 12),
        onComplete = {},
    )
}

@ComposePreview(showBackground = true, widthDp = 390, heightDp = 844, name = "인화 연출 - 내려오는 중")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryPrintAnimationRollingPreview() {
    FilmDispensingStage(
        photos = previewGalleryPhotos(count = 12),
        rolls = true,
        onPulled = {},
        onRolled = {},
    )
}

@ComposePreview(showBackground = true, widthDp = 390, heightDp = 844, name = "인화 연출 - 사진 등장")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryPrintAnimationRevealingPreview() {
    PhotoRevealStage(
        photos = previewGalleryPhotos(count = 12),
        gridState = rememberLazyGridState(),
        extraBottomPadding = 0.dp,
        onComplete = {},
    )
}
