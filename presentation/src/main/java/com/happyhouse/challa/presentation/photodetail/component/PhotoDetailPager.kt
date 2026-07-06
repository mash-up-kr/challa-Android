package com.happyhouse.challa.presentation.photodetail.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailUiModel
import com.happyhouse.challa.presentation.photodetail.previewPhotoDetailPhotos
import kotlinx.collections.immutable.ImmutableList
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
fun PhotoDetailPager(
    photos: ImmutableList<PhotoDetailUiModel>,
    initialPhotoId: Long,
    onSaveClick: (PhotoDetailUiModel) -> Unit,
    modifier: Modifier = Modifier,
    isSaving: Boolean = false,
) {
    val initialPage = photos.indexOfFirst { it.id == initialPhotoId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { photos.size }

    Box(modifier = modifier) {
        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
            key = { page -> photos[page].id },
        ) { page ->
            PhotoDetailPage(
                modifier = Modifier.fillMaxSize(),
                photo = photos[page],
            )
        }

        TextButton(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
            enabled = !isSaving,
            onClick = { onSaveClick(photos[pagerState.currentPage]) },
        ) {
            Text(text = stringResource(R.string.photo_detail_save))
        }
    }
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF000000)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPagerPreview() {
    PhotoDetailPager(
        modifier = Modifier.fillMaxSize(),
        photos = previewPhotoDetailPhotos(),
        initialPhotoId = 0L,
        onSaveClick = {},
    )
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF000000, name = "PhotoDetailPager - Saving")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPagerSavingPreview() {
    PhotoDetailPager(
        modifier = Modifier.fillMaxSize(),
        photos = previewPhotoDetailPhotos(),
        initialPhotoId = 0L,
        isSaving = true,
        onSaveClick = {},
    )
}
