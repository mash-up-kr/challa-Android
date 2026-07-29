package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import com.happyhouse.challa.presentation.gallery.contract.GalleryIntent
import com.happyhouse.challa.presentation.gallery.contract.GalleryPhotoUiModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryState
import com.happyhouse.challa.presentation.gallery.contract.GalleryState.PhotoInfo
import kotlinx.collections.immutable.toPersistentList
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

/**
 * 갤러리 본문
 * 로딩/에러/빈/인화 전/인화 완료 분기
 */
@Composable
fun GalleryContent(
    state: GalleryState,
    onIntent: (GalleryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        when (val photoInfo = state.photoInfo) {
            PhotoInfo.Loading -> {
                GalleryCenterBox {
                    CircularProgressIndicator()
                }
            }

            PhotoInfo.Error -> {
                GalleryCenterBox {
                    GalleryMessage(
                        message = stringResource(R.string.gallery_error_message),
                        actionLabel = stringResource(R.string.gallery_retry),
                        onAction = { onIntent(GalleryIntent.PhotosLoad) },
                    )
                }
            }

            PhotoInfo.Empty -> {
                GalleryCenterBox {
                    GalleryMessage(
                        message = stringResource(R.string.gallery_empty),
                    )
                }
            }

            is PhotoInfo.Waiting -> {
                GalleryFilmSlotGrid(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    slotCount = photoInfo.slotCount,
                )
            }

            is PhotoInfo.Printed -> {
                GalleryPhotoGrid(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    photos = photoInfo.photos,
                    onPhotoClick = { photoId -> onIntent(GalleryIntent.PhotoClick(photoId)) },
                )
            }
        }
    }
}

/**
 * 로딩/에러/빈 상태를 화면 중앙에 배치하는 영역
 */
@Composable
private fun ColumnScope.GalleryCenterBox(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier =
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
private fun GalleryMessage(
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
        Text(text = message)
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(text = actionLabel)
            }
        }
    }
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF111111, widthDp = 390, name = "Gallery - 인화 전")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryContentWaitingPreview() {
    GalleryContent(
        modifier = Modifier.fillMaxSize(),
        state =
            GalleryState(
                roomName = "친구들과 강릉 여행",
                photoInfo = PhotoInfo.Waiting(slotCount = 24),
            ),
        onIntent = {},
    )
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF111111, widthDp = 390, name = "Gallery - 인화 완료")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryContentPrintedPreview() {
    val photos =
        (0 until 24)
            .map { index ->
                GalleryPhotoUiModel(
                    id = index.toLong(),
                    order = index + 1,
                    imageUrl = "",
                )
            }.toPersistentList()

    GalleryContent(
        modifier = Modifier.fillMaxSize(),
        state =
            GalleryState(
                roomName = "친구들과 강릉 여행",
                photoInfo = PhotoInfo.Printed(photos),
            ),
        onIntent = {},
    )
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF111111, name = "Gallery - Empty")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryContentEmptyPreview() {
    GalleryContent(
        modifier = Modifier.fillMaxSize(),
        state =
            GalleryState(
                roomName = "친구들과 강릉 여행",
                photoInfo = PhotoInfo.Empty,
            ),
        onIntent = {},
    )
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF111111, name = "Gallery - Loading")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryContentLoadingPreview() {
    GalleryContent(
        modifier = Modifier.fillMaxSize(),
        state =
            GalleryState(
                roomName = "친구들과 강릉 여행",
                photoInfo = PhotoInfo.Loading,
            ),
        onIntent = {},
    )
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF111111, name = "Gallery - Error")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryContentErrorPreview() {
    GalleryContent(
        modifier = Modifier.fillMaxSize(),
        state =
            GalleryState(
                roomName = "친구들과 강릉 여행",
                photoInfo = PhotoInfo.Error,
            ),
        onIntent = {},
    )
}
