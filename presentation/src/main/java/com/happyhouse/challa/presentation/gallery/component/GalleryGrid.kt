package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaCardItem
import com.happyhouse.challa.presentation.designsystem.component.ChallaCardType
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.gallery.contract.GalleryFilmSlotUiModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryPhotoUiModel
import com.happyhouse.challa.presentation.gallery.previewGalleryFilmSlots
import com.happyhouse.challa.presentation.gallery.previewGalleryPhotos
import kotlinx.collections.immutable.ImmutableList
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private const val GALLERY_COLUMN_COUNT = 4

private val GalleryGridSpacing = 10.dp
private val GalleryGridHorizontalPadding = 16.dp
private val GalleryGridVerticalPadding = 20.dp

/**
 * 인화 전: 번호만 있는 빈 필름 슬롯 그리드
 *
 * @param state 인화 완료 그리드와 같은 것을 넘기면 전환해도 스크롤 위치가 유지된다.
 * @param extraBottomPadding 위에 떠 있는 하단 바에 마지막 줄이 가리지 않도록 더하는 여백
 */
@Composable
fun GalleryFilmSlotGrid(
    slots: ImmutableList<GalleryFilmSlotUiModel>,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    extraBottomPadding: Dp = 0.dp,
) {
    GalleryGridLayout(
        modifier = modifier,
        state = state,
        extraBottomPadding = extraBottomPadding,
    ) {
        items(
            items = slots,
            key = { slot -> slot.order },
        ) { slot ->
            val imageUrl = slot.imageUrl

            ChallaCardItem(
                order = slot.order,
                type = imageUrl?.let(ChallaCardType::PrintWaiting) ?: ChallaCardType.NotCaptured,
                contentDescription =
                    if (imageUrl == null) {
                        stringResource(R.string.gallery_empty_slot_description, slot.order)
                    } else {
                        stringResource(R.string.gallery_film_slot_description, slot.order)
                    },
            )
        }
    }
}

/**
 * 인화 완료: 공개된 방 사진 그리드
 *
 * @param state 인화 전 그리드와 같은 것을 넘기면 전환해도 스크롤 위치가 유지된다.
 * @param extraBottomPadding 위에 떠 있는 하단 바에 마지막 줄이 가리지 않도록 더하는 여백
 */
@Composable
fun GalleryPhotoGrid(
    photos: ImmutableList<GalleryPhotoUiModel>,
    onPhotoClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    extraBottomPadding: Dp = 0.dp,
) {
    GalleryGridLayout(
        modifier = modifier,
        state = state,
        extraBottomPadding = extraBottomPadding,
    ) {
        items(
            items = photos,
            key = { photo -> photo.id },
        ) { photo ->
            ChallaCardItem(
                order = photo.order,
                type = ChallaCardType.Printed(imageUrl = photo.imageUrl),
                contentDescription = stringResource(R.string.gallery_photo_content_description, photo.order),
                onClickLabel = stringResource(R.string.gallery_open_photo),
                onClick = { onPhotoClick(photo.id) },
            )
        }
    }
}

@Composable
private fun GalleryGridLayout(
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    extraBottomPadding: Dp = 0.dp,
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        modifier = modifier,
        state = state,
        columns = GridCells.Fixed(GALLERY_COLUMN_COUNT),
        contentPadding =
            PaddingValues(
                start = GalleryGridHorizontalPadding,
                end = GalleryGridHorizontalPadding,
                top = GalleryGridVerticalPadding,
                bottom = GalleryGridVerticalPadding + extraBottomPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(GalleryGridSpacing),
        verticalArrangement = Arrangement.spacedBy(GalleryGridSpacing),
        content = content,
    )
}

@ComposePreview(showBackground = true, widthDp = 390)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryFilmSlotGridPreview() {
    GalleryFilmSlotGrid(slots = previewGalleryFilmSlots())
}

@ComposePreview(showBackground = true, widthDp = 390)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryPhotoGridPreview() {
    GalleryPhotoGrid(
        photos = previewGalleryPhotos(count = 12),
        onPhotoClick = {},
    )
}
