package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
 * @param extraBottomPadding 위에 떠 있는 하단 바에 마지막 줄이 가리지 않도록 더하는 여백
 */
@Composable
fun GalleryFilmSlotGrid(
    slots: ImmutableList<GalleryFilmSlotUiModel>,
    modifier: Modifier = Modifier,
    extraBottomPadding: Dp = 0.dp,
) {
    GalleryGridLayout(
        modifier = modifier,
        extraBottomPadding = extraBottomPadding,
    ) {
        items(
            items = slots,
            key = { slot -> slot.order },
        ) { slot ->
            GalleryFilmCard(slot = slot)
        }
    }
}

/**
 * 인화 완료: 공개된 방 사진 그리드
 *
 * @param extraBottomPadding 위에 떠 있는 하단 바에 마지막 줄이 가리지 않도록 더하는 여백
 */
@Composable
fun GalleryPhotoGrid(
    photos: ImmutableList<GalleryPhotoUiModel>,
    onPhotoClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    extraBottomPadding: Dp = 0.dp,
) {
    GalleryGridLayout(
        modifier = modifier,
        extraBottomPadding = extraBottomPadding,
    ) {
        items(
            items = photos,
            key = { photo -> photo.id },
        ) { photo ->
            GalleryPhotoItem(
                photo = photo,
                onClick = { onPhotoClick(photo.id) },
            )
        }
    }
}

@Composable
private fun GalleryGridLayout(
    modifier: Modifier = Modifier,
    extraBottomPadding: Dp = 0.dp,
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        modifier = modifier,
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

@ComposePreview(showBackground = true, backgroundColor = 0xFF111111, widthDp = 390)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryFilmSlotGridPreview() {
    GalleryFilmSlotGrid(slots = previewGalleryFilmSlots())
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF111111, widthDp = 390)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryPhotoGridPreview() {
    GalleryPhotoGrid(
        photos = previewGalleryPhotos(count = 12),
        onPhotoClick = {},
    )
}
