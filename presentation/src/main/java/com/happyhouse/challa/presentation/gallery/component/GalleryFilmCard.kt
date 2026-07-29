package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.transformations
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.CHALLA_PREVIEW_BACKGROUND
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.gallery.contract.GalleryFilmSlotUiModel
import com.happyhouse.challa.presentation.util.BlurTransformation
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private const val FILM_CARD_ASPECT_RATIO = 3f / 4f

private val FilmCardShape = RoundedCornerShape(10.dp)

// TODO: 디자인 토큰에 없는 값이라 컴포넌트 로컬 상수로 둔다. 토큰 추가되면 교체
private val FilmCardEmptyColor = Color.White.copy(alpha = 0.05f)

/**
 * 인화 전 필름 슬롯 1칸
 *
 * 아직 공개 전이라 사진이 있어도 흐리게 덮어 번호만 또렷하게 보인다.
 */
@Composable
fun GalleryFilmCard(
    slot: GalleryFilmSlotUiModel,
    modifier: Modifier = Modifier,
) {
    val slotDescription = stringResource(R.string.gallery_film_slot_description, slot.order)

    Box(
        modifier =
            modifier
                // 번호 텍스트만 따로 읽히지 않도록 카드 전체를 한 덩어리로 읽힌다.
                .semantics(mergeDescendants = true) { contentDescription = slotDescription }
                .aspectRatio(FILM_CARD_ASPECT_RATIO)
                .clip(FilmCardShape)
                .background(FilmCardEmptyColor)
                .border(
                    width = 1.dp,
                    color = ChallaTheme.colors.lineNeutral,
                    shape = FilmCardShape,
                ),
    ) {
        if (slot.imageUrl != null) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(slot.imageUrl)
                        // TODO: 서버가 인화 전용 블러 이미지를 내려주면 이중 블러가 되므로 제거할 것.
                        .transformations(BlurTransformation())
                        .crossfade(true)
                        .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(FilmCardEmptyColor),
            )
        }

        Text(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 10.dp, bottom = 10.dp),
            text = slot.order.toString(),
            color = ChallaTheme.colors.labelSubtle,
            style = ChallaTheme.typography.bodyLarge.bold,
        )
    }
}

@ComposePreview(showBackground = true, backgroundColor = CHALLA_PREVIEW_BACKGROUND)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryFilmCardPreview() {
    GalleryFilmCard(
        modifier = Modifier.width(82.dp),
        slot = GalleryFilmSlotUiModel(order = 1, imageUrl = null),
    )
}
