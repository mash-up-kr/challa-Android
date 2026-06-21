package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

/**
 * 갤러리 본문
 * 상단 D-day 배너 + (로딩/에러/빈/그리드) 분기
 */
@Composable
fun GalleryContent(
    state: GalleryState,
    onIntent: (GalleryIntent) -> Unit,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        GalleryTopBar(
            modifier = Modifier.fillMaxWidth(),
            title = state.roomName,
            onBackClick = onBackClick,
            onMoreClick = onMoreClick,
        )

        GalleryExpiryBanner(
            modifier = Modifier.fillMaxWidth(),
            remainingDays = state.remainingDays,
        )

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.isError -> {
                    GalleryMessage(
                        modifier = Modifier.align(Alignment.Center),
                        message = stringResource(R.string.gallery_error_message),
                        actionLabel = stringResource(R.string.gallery_retry),
                        onAction = { onIntent(GalleryIntent.PhotosLoad) },
                    )
                }

                state.isEmpty -> {
                    GalleryMessage(
                        modifier = Modifier.align(Alignment.Center),
                        message = stringResource(R.string.gallery_empty),
                    )
                }

                else -> {
                    GalleryGrid(
                        modifier = Modifier.fillMaxSize(),
                        photos = state.photos,
                        onPhotoClick = { photoId -> onIntent(GalleryIntent.PhotoClick(photoId)) },
                    )
                }
            }
        }
    }
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

@ComposePreview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryContentPreview() {
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
                roomName = "다낭 4박5일",
                isLoading = false,
                photos = photos,
                remainingDays = 3,
            ),
        onIntent = {},
        onBackClick = {},
        onMoreClick = {},
    )
}

@ComposePreview(showBackground = true, name = "Gallery - Empty")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryContentEmptyPreview() {
    GalleryContent(
        modifier = Modifier.fillMaxSize(),
        state =
            GalleryState(
                roomName = "다낭 4박5일",
                isLoading = false,
                photos = persistentListOf(),
                remainingDays = 3,
            ),
        onIntent = {},
        onBackClick = {},
        onMoreClick = {},
    )
}
