package com.happyhouse.challa.presentation.photodetail.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState.PhotoInfo
import com.happyhouse.challa.presentation.photodetail.previewPhotoDetailPhotos
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private val PhotoHorizontalPadding = 16.dp
private val IndicatorTopPadding = 16.dp

private val PhotoPageSpacing = PhotoHorizontalPadding * 2

@Composable
fun PhotoDetailPager(
    loaded: PhotoInfo.Loaded,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val photos = loaded.photos

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(PhotoCardHeight),
            state = pagerState,
            contentPadding = PaddingValues(horizontal = PhotoHorizontalPadding),
            pageSpacing = PhotoPageSpacing,
            key = { page -> photos[page].id },
        ) { page ->
            val photo = photos[page]

            PhotoDetailPage(
                modifier = Modifier.fillMaxSize(),
                photo = photo,
                reactions = loaded.reactionsOf(photo.id),
            )
        }

        PhotoDetailPageIndicator(
            modifier = Modifier.padding(top = IndicatorTopPadding),
            pagerState = pagerState,
        )
    }
}

@ComposePreview(showBackground = true, heightDp = 730)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPagerPreview() {
    val photos = previewPhotoDetailPhotos(count = 24)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        PhotoDetailPager(
            modifier = Modifier.fillMaxWidth(),
            loaded = PhotoInfo.Loaded(photos),
            pagerState = rememberPagerState { photos.size },
        )
    }
}

@ComposePreview(
    showBackground = true,
    heightDp = 730,
    name = "PhotoDetailPager - 사진 1장",
)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPagerSinglePhotoPreview() {
    val photos = previewPhotoDetailPhotos(count = 1)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        PhotoDetailPager(
            modifier = Modifier.fillMaxWidth(),
            loaded = PhotoInfo.Loaded(photos),
            pagerState = rememberPagerState { photos.size },
        )
    }
}
