package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.gallery.contract.GalleryPhotoUiModel
import com.happyhouse.challa.presentation.gallery.previewGalleryPhotos
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

/** 필름이 나오는 입구 */
private val DispenserTopPadding = 12.dp
private val DispenserHeight = 24.dp
private val DispenserHorizontalPadding = 20.dp
private val DispenserBorderWidth = 2.dp

/** 필름 한 칸. 인화 전 카드(세로 3:4)와 달리 필름은 가로로 눕는다. */
private const val FILM_CELL_ASPECT_RATIO = 4f / 3f
private const val FILM_STRIP_WIDTH_RATIO = 0.55f
private val FilmSprocketAreaWidth = 12.dp
private val FilmCellSpacing = 6.dp
private val FilmSprocketSize = 5.dp
private val FilmSprocketCornerRadius = 1.dp
private val FilmSprocketPitch = 14.dp

/** 당김 대기에서 첫 칸이 배출구 밖으로 나와 있는 높이 */
private val FilmPeekHeight = 84.dp
private val PullHintTopOffset = FilmPeekHeight + 24.dp

/** 당기고 싶게 유도하는 모션의 진폭 */
private val PullHintBounceDistance = 10.dp
private const val PULL_HINT_BOUNCE_MS = 900

/** 이만큼 당기면 나머지는 자동으로 흘러내린다. */
private val PullTriggerDistance = 56.dp

/** 필름이 흘러내리는 시간. 칸 수에 비례하되 너무 짧거나 지루하지 않게 자른다. */
private const val ROLL_MS_PER_PHOTO = 110
private const val ROLL_MIN_MS = 1_400
private const val ROLL_MAX_MS = 4_000

/** 사진이 한 장씩 나타나는 간격 */
private const val REVEAL_STAGGER_MS = 45L

/** 필름이 다 내려온 뒤 그리드로 넘어가기까지의 사이 */
private const val ROLL_TO_REVEAL_DELAY_MS = 200L

/** 연출 진행 단계. 화면 안에서만 쓰이므로 State에 두지 않는다. */
private enum class PrintAnimationPhase {
    /** 필름 첫 칸만 나온 채 사용자가 당기기를 기다린다. */
    PULL_WAITING,

    /** 한 번 당긴 뒤 나머지가 자동으로 흘러내린다. */
    ROLLING,

    /** 그리드로 바뀌어 사진이 1번부터 하나씩 나타난다. */
    REVEALING,
}

/**
 * 인화 완료 연출. 필름을 당겨 뽑으면 사진이 그리드에 하나씩 나타난다.
 *
 * 연출이 끝나면 [onComplete]로 알리고, 이후에는 [GalleryPhotoGrid]만 남는다.
 *
 * @param photos 연출에 쓸 사진. 비어 있으면 보여줄 필름이 없어 바로 [onComplete]한다.
 * @param gridState 연출이 끝난 뒤 남는 그리드와 같은 것을 넘기면 스크롤 위치가 이어진다.
 * @param onFilmStageChange 필름이 배출구에서 나오는 동안 true. 프로필 바 자리를 배출구가 쓰므로
 *   그동안만 프로필 바를 비운다. 사진이 나타나는 단계에는 다시 보여야 해서 화면에 알린다.
 */
@Composable
fun GalleryPrintAnimation(
    photos: ImmutableList<GalleryPhotoUiModel>,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    onFilmStageChange: (Boolean) -> Unit = {},
) {
    var phase by remember { mutableStateOf(PrintAnimationPhase.PULL_WAITING) }

    val showsFilm =
        phase == PrintAnimationPhase.PULL_WAITING || phase == PrintAnimationPhase.ROLLING
    LaunchedEffect(showsFilm) { onFilmStageChange(showsFilm) }

    // 화면을 벗어날 때 프로필 바를 되돌려 놓는다. 연출 도중 나가면 그대로 비어 있게 된다.
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
                onComplete = onComplete,
            )
    }
}

/**
 * 배출구에서 필름이 나와 아래로 흘러내리는 단계
 *
 * @param rolls true면 당김이 끝나 자동으로 흘러내린다.
 */
@Composable
private fun FilmDispensingStage(
    photos: ImmutableList<GalleryPhotoUiModel>,
    rolls: Boolean,
    onPulled: () -> Unit,
    onRolled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        FilmDispenser(
            modifier =
                Modifier
                    .padding(
                        top = DispenserTopPadding,
                        start = DispenserHorizontalPadding,
                        end = DispenserHorizontalPadding,
                    ).fillMaxWidth(),
        )

        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    // 아직 나오지 않은 칸과 다 빠져나간 칸이 밖에 그려지지 않게 자른다.
                    .clipToBounds(),
        ) {
            val density = LocalDensity.current
            val stripWidth = maxWidth * FILM_STRIP_WIDTH_RATIO
            val cellWidth = stripWidth - FilmSprocketAreaWidth * 2
            val cellHeight = cellWidth / FILM_CELL_ASPECT_RATIO

            // 칸 하나가 차지하는 간격. 칸 사이 여백까지 포함해 필름을 균일하게 잇는다.
            val cellPitch = cellHeight + FilmCellSpacing
            val cellPitchPx = with(density) { cellPitch.toPx() }
            val viewportPx = with(density) { maxHeight.toPx() }

            // 시작: 맨 아래 칸(1번 사진)만 배출구 밖으로 나와 있다.
            // 끝: 필름 전체가 화면 아래로 빠져나간다.
            val filmTopStartPx = with(density) { FilmPeekHeight.toPx() } - cellPitchPx * photos.size
            val travelPx = viewportPx - filmTopStartPx
            val pullTriggerPx = with(density) { PullTriggerDistance.toPx() }
            val bounceDistancePx = with(density) { PullHintBounceDistance.toPx() }

            val progress = remember { Animatable(0f) }
            val bounce = pullHintBounce(enabled = !rolls)
            val scope = rememberCoroutineScope()

            LaunchedEffect(rolls) {
                if (!rolls) return@LaunchedEffect

                val durationMillis =
                    (photos.size * ROLL_MS_PER_PHOTO).coerceIn(ROLL_MIN_MS, ROLL_MAX_MS)
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing),
                )
                onRolled()
            }

            // 필름을 한 덩어리로 그리면 방이 클수록 레이어가 화면의 몇 배로 커져
            // 기기가 감당하지 못하고 통째로 사라진다. 칸마다 따로 그리고 위치만 옮긴다.
            // 아래로 흘러내리며 1번 사진부터 보이도록 뒤에서부터 쌓는다.
            photos.indices.forEach { index ->
                val photo = photos[photos.lastIndex - index]

                FilmCell(
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            // 위치를 배치 단계에서 읽어, 1초에 수십 번 다시 구성되지 않게 한다.
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
                Box(
                    modifier =
                        Modifier
                            .matchParentSize()
                            .draggable(
                                orientation = Orientation.Vertical,
                                state =
                                    rememberDraggableState { delta ->
                                        // 위로 미는 동작은 무시한다. 되감는 연출은 스펙에 없다.
                                        if (delta <= 0f) return@rememberDraggableState

                                        val next = progress.value + delta / travelPx
                                        scope.launch { progress.snapTo(next) }
                                        if (next * travelPx >= pullTriggerPx) onPulled()
                                    },
                            ),
                )

                Text(
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = PullHintTopOffset),
                    text = stringResource(R.string.gallery_print_animation_pull_hint),
                    textAlign = TextAlign.Center,
                    color = ChallaTheme.colors.labelNeutral,
                    style = ChallaTheme.typography.bodyMedium.medium,
                )
            }
        }
    }
}

/**
 * 필름 한 칸. 칸 사이 여백까지 검은 필름으로 채워 위아래 칸과 이어 붙는다.
 *
 * @param cellPitch 칸 하나가 차지하는 높이. [cellHeight]와의 차이가 다음 칸과의 여백이 된다.
 */
@Composable
private fun FilmCell(
    imageUrl: String,
    stripWidth: Dp,
    cellWidth: Dp,
    cellHeight: Dp,
    cellPitch: Dp,
    modifier: Modifier = Modifier,
) {
    val sprocketColor = ChallaTheme.colors.backgroundLevel3

    Box(
        modifier =
            modifier
                .width(stripWidth)
                .height(cellPitch)
                .background(ChallaTheme.colors.staticBlack)
                .drawBehind { drawFilmSprockets(color = sprocketColor) },
    ) {
        AsyncImage(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .width(cellWidth)
                    .height(cellHeight),
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
            // 필름은 한 덩어리로 읽히면 되므로 칸마다 읽지 않는다.
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}

/** 그리드로 바뀌어 1번 사진부터 하나씩 나타나는 단계 */
@Composable
private fun PhotoRevealStage(
    photos: ImmutableList<GalleryPhotoUiModel>,
    gridState: LazyGridState,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var revealedCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(photos.size) {
        delay(ROLL_TO_REVEAL_DELAY_MS)
        while (revealedCount < photos.size) {
            revealedCount++
            delay(REVEAL_STAGGER_MS)
        }
        onComplete()
    }

    GalleryPhotoGrid(
        modifier = modifier,
        photos = photos,
        state = gridState,
        revealedCount = revealedCount,
        // 연출 중에는 스크롤과 사진 열기를 막는다. 다음 페이지는 연출 전에 미리 받아둔다.
        userScrollEnabled = false,
        onPhotoClick = {},
        onLoadMore = {},
    )
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
                    animation = tween(durationMillis = PULL_HINT_BOUNCE_MS, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "GalleryPrintPullHintOffset",
        )

    // 흘러내리는 동안에는 흔들 이유가 없다. 트랜지션은 살려두고 진폭만 0으로 둔다.
    return remember(enabled, bounce) {
        derivedStateOf { if (enabled) bounce.value else 0f }
    }
}

/** 필름이 나오는 입구 */
@Composable
private fun FilmDispenser(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(percent = 50)

    Box(
        modifier =
            modifier
                .height(DispenserHeight)
                .clip(shape)
                .background(ChallaTheme.colors.backgroundLevel1)
                .border(width = DispenserBorderWidth, color = ChallaTheme.colors.primary, shape = shape),
    )
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
