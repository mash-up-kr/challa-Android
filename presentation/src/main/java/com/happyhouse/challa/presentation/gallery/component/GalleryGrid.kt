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
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.gallery.contract.GalleryPhotoUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private const val GALLERY_COLUMN_COUNT = 4

private val GalleryGridSpacing = 10.dp
private val GalleryGridContentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)

/**
 * 인화 전: 번호만 있는 빈 필름 슬롯 그리드
 */
@Composable
fun GalleryFilmSlotGrid(
    slotCount: Int,
    modifier: Modifier = Modifier,
) {
    GalleryGridLayout(modifier = modifier) {
        items(count = slotCount) { index ->
            GalleryFilmCard(order = index + 1)
        }
    }
}

/**
 * 인화 완료: 공개된 방 사진 그리드
 */
@Composable
fun GalleryPhotoGrid(
    photos: ImmutableList<GalleryPhotoUiModel>,
    onPhotoClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    GalleryGridLayout(modifier = modifier) {
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
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(GALLERY_COLUMN_COUNT),
        contentPadding = GalleryGridContentPadding,
        horizontalArrangement = Arrangement.spacedBy(GalleryGridSpacing),
        verticalArrangement = Arrangement.spacedBy(GalleryGridSpacing),
        content = content,
    )
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF111111, widthDp = 390)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryFilmSlotGridPreview() {
    GalleryFilmSlotGrid(slotCount = 24)
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF111111, widthDp = 390)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryPhotoGridPreview() {
    val photos =
        (0 until 12)
            .map { index ->
                GalleryPhotoUiModel(
                    id = index.toLong(),
                    order = index + 1,
                    imageUrl = "",
                )
            }.toPersistentList()

    GalleryPhotoGrid(
        photos = photos,
        onPhotoClick = {},
    )
}
