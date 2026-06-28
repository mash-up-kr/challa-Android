package com.happyhouse.challa.presentation.photodetail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailIntent
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState.PhotoInfo
import com.happyhouse.challa.presentation.photodetail.previewPhotoDetailPhotos
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
fun PhotoDetailContent(
    state: PhotoDetailState,
    onIntent: (PhotoDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when (val photoInfo = state.photoInfo) {
            PhotoInfo.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            PhotoInfo.Error -> {
                PhotoDetailMessage(
                    modifier = Modifier.align(Alignment.Center),
                    message = stringResource(R.string.photo_detail_error_message),
                    actionLabel = stringResource(R.string.photo_detail_retry),
                    onAction = { onIntent(PhotoDetailIntent.PhotosLoad) },
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
                    modifier = Modifier.fillMaxSize(),
                    photos = photoInfo.photos,
                    initialPhotoId = state.initialPhotoId,
                    onSaveClick = { imageUrl -> onIntent(PhotoDetailIntent.PhotoSave(imageUrl)) },
                )
            }
        }
    }
}

@Composable
private fun PhotoDetailMessage(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = message, color = Color.White)
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(text = actionLabel)
            }
        }
    }
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF000000)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailContentLoadedPreview() {
    PhotoDetailContent(
        modifier = Modifier.fillMaxSize(),
        state =
            PhotoDetailState(
                roomName = "길고양이를찍으러가자",
                initialPhotoId = 0L,
                photoInfo = PhotoInfo.Loaded(previewPhotoDetailPhotos()),
            ),
        onIntent = {},
    )
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF000000, name = "PhotoDetail - Loading")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailContentLoadingPreview() {
    PhotoDetailContent(
        modifier = Modifier.fillMaxSize(),
        state = PhotoDetailState(photoInfo = PhotoInfo.Loading),
        onIntent = {},
    )
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF000000, name = "PhotoDetail - Error")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailContentErrorPreview() {
    PhotoDetailContent(
        modifier = Modifier.fillMaxSize(),
        state = PhotoDetailState(photoInfo = PhotoInfo.Error),
        onIntent = {},
    )
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF000000, name = "PhotoDetail - Empty")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailContentEmptyPreview() {
    PhotoDetailContent(
        modifier = Modifier.fillMaxSize(),
        state = PhotoDetailState(photoInfo = PhotoInfo.Empty),
        onIntent = {},
    )
}
