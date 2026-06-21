package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.gallery.contract.GalleryPhotoUiModel
import kotlinx.collections.immutable.ImmutableList

private const val GALLERY_COLUMN_COUNT = 4
private const val GALLERY_ITEM_CONTENT_TYPE = "gallery_photo"

/**
 * 공개된 방 사진 4열 그리드
 */
@Composable
fun GalleryGrid(
    photos: ImmutableList<GalleryPhotoUiModel>,
    onPhotoClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(GALLERY_COLUMN_COUNT),
        contentPadding = PaddingValues(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(
            items = photos,
            key = { photo -> photo.id },
            contentType = { GALLERY_ITEM_CONTENT_TYPE },
        ) { photo ->
            GalleryPhotoItem(
                photo = photo,
                onClick = { onPhotoClick(photo.id) },
            )
        }
    }
}
