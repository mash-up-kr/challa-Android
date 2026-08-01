package com.happyhouse.challa.presentation.photodetail.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonSize
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonVariant
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaTextButton
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
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when (val photoInfo = state.photoInfo) {
            PhotoInfo.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = ChallaTheme.colors.primary,
                )
            }

            PhotoInfo.Error -> {
                ChallaTextButton(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.photo_detail_retry),
                    onClick = onRetryClick,
                    variant = ChallaButtonVariant.NEUTRAL,
                    size = ChallaButtonSize.MEDIUM,
                )
            }

            PhotoInfo.Empty -> {
                PhotoDetailMessage(
                    modifier = Modifier.align(Alignment.Center),
                    message = stringResource(R.string.photo_detail_empty),
                )
            }

            is PhotoInfo.Loaded -> {
                PhotoDetailPager(
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(),
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
        onRetryClick = {},
    )
}

@ComposePreview(showBackground = true, heightDp = 730, name = "PhotoDetail - Loading")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailContentLoadingPreview() {
    PhotoDetailContent(
        modifier = Modifier.fillMaxSize(),
        state = PhotoDetailState(photoInfo = PhotoInfo.Loading),
        pagerState = rememberPagerState { 0 },
        onRetryClick = {},
    )
}

@ComposePreview(showBackground = true, heightDp = 730, name = "PhotoDetail - Error")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailContentErrorPreview() {
    PhotoDetailContent(
        modifier = Modifier.fillMaxSize(),
        state = PhotoDetailState(photoInfo = PhotoInfo.Error),
        pagerState = rememberPagerState { 0 },
        onRetryClick = {},
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
        onRetryClick = {},
    )
}
