package com.happyhouse.challa.presentation.photodetail.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState.PhotoInfo
import com.happyhouse.challa.presentation.photodetail.previewPhotoDetailPhotos
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
fun PhotoDetailContent(
    state: PhotoDetailState,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when (val photoInfo = state.photoInfo) {
            PhotoInfo.Empty -> {
                PhotoDetailMessage(
                    modifier = Modifier.align(Alignment.Center),
                    message = stringResource(R.string.photo_detail_empty),
                )
            }

            is PhotoInfo.Loaded -> {
                // 카드 높이를 남는 공간에 맞춰 줄여야 해서 높이를 다 넘긴다. 가운데 정렬은 Pager가 한다.
                PhotoDetailPager(
                    modifier = Modifier.fillMaxSize(),
                    loaded = photoInfo,
                    pagerState = pagerState,
                )
            }
        }
    }
}

@Composable
private fun PhotoDetailMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier.padding(24.dp),
        text = message,
        color = ChallaTheme.colors.labelNormal,
        style = ChallaTheme.typography.bodyMedium.medium,
    )
}

@ComposePreview(showBackground = true, heightDp = 730)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailContentLoadedPreview() {
    val photos = previewPhotoDetailPhotos(count = 24)

    PhotoDetailContent(
        modifier = Modifier.fillMaxSize(),
        state =
            PhotoDetailState(
                roomName = "해피하우스 강릉 여행",
                initialPhotoId = 0L,
                photoInfo = PhotoInfo.Loaded(photos),
            ),
        pagerState = rememberPagerState { photos.size },
    )
}

@ComposePreview(showBackground = true, heightDp = 730, name = "PhotoDetail - Empty")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailContentEmptyPreview() {
    PhotoDetailContent(
        modifier = Modifier.fillMaxSize(),
        state = PhotoDetailState(photoInfo = PhotoInfo.Empty),
        pagerState = rememberPagerState { 0 },
    )
}
