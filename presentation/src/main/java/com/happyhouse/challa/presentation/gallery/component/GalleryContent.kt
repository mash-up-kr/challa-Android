package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.CHALLA_PREVIEW_BACKGROUND
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.gallery.PREVIEW_REMAINING_SECONDS
import com.happyhouse.challa.presentation.gallery.contract.GalleryIntent
import com.happyhouse.challa.presentation.gallery.contract.GalleryState
import com.happyhouse.challa.presentation.gallery.contract.GalleryState.PhotoInfo
import com.happyhouse.challa.presentation.gallery.previewGalleryFilmSlots
import com.happyhouse.challa.presentation.gallery.previewGalleryMembers
import com.happyhouse.challa.presentation.gallery.previewGalleryPhotos
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

/**
 * 갤러리 본문
 * 로딩/에러/빈/인화 전/인화 완료 분기
 *
 * @param extraBottomPadding 위에 떠 있는 하단 바에 그리드 마지막 줄이 가리지 않도록 더하는 여백
 */
@Composable
fun GalleryContent(
    state: GalleryState,
    onIntent: (GalleryIntent) -> Unit,
    modifier: Modifier = Modifier,
    extraBottomPadding: Dp = 0.dp,
) {
    Box(modifier = modifier) {
        // 인화 전/후 그리드는 서로 다른 LazyVerticalGrid라, 상태를 공유하지 않으면
        // 전환할 때 스크롤이 맨 위로 튄다. 두 그리드의 칸 수와 배치가 같아 그대로 이어진다.
        val gridState = rememberLazyGridState()

        AnimatedContent(
            targetState = state.photoInfo,
            // 남은 시간이 1초마다 바뀌어도 다시 그리지 않도록 타입만 키로 쓴다.
            contentKey = { photoInfo -> photoInfo::class },
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "GalleryPhotoInfo",
        ) { photoInfo ->
            when (photoInfo) {
                PhotoInfo.Loading -> {
                    GalleryCenterBox {
                        CircularProgressIndicator(color = ChallaTheme.colors.labelNormal)
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
                        modifier = Modifier.fillMaxSize(),
                        slots = photoInfo.slots,
                        state = gridState,
                        extraBottomPadding = extraBottomPadding,
                    )
                }

                is PhotoInfo.Printed -> {
                    GalleryPhotoGrid(
                        modifier = Modifier.fillMaxSize(),
                        photos = photoInfo.photos,
                        onPhotoClick = { photoId -> onIntent(GalleryIntent.PhotoClick(photoId)) },
                        state = gridState,
                        extraBottomPadding = extraBottomPadding,
                    )
                }
            }
        }

        // 그리드가 크로스페이드되는 동안 겹쳐 보이지 않도록 프로필 바는 밖에 둔다.
        val showsProfileBar =
            when (state.photoInfo) {
                is PhotoInfo.Waiting, is PhotoInfo.Printed -> true
                PhotoInfo.Loading, PhotoInfo.Error, PhotoInfo.Empty -> false
            }
        if (showsProfileBar) {
            GalleryProfileBar(
                modifier = Modifier.align(Alignment.TopCenter),
                members = state.members,
            )
        }
    }
}

/**
 * 로딩/에러/빈 상태를 화면 중앙에 배치하는 영역
 */
@Composable
private fun GalleryCenterBox(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
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
        Text(
            text = message,
            color = ChallaTheme.colors.labelNormal,
            style = ChallaTheme.typography.bodyMedium.medium,
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionLabel,
                    color = ChallaTheme.colors.primaryYellow,
                    style = ChallaTheme.typography.bodyMedium.bold,
                )
            }
        }
    }
}

@ComposePreview(
    showBackground = true,
    backgroundColor = CHALLA_PREVIEW_BACKGROUND,
    widthDp = 390,
    name = "Gallery - 인화 전",
)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryContentWaitingPreview() {
    GalleryContentPreviewTemplate(
        photoInfo = PhotoInfo.Waiting(slots = previewGalleryFilmSlots(), remainingSeconds = PREVIEW_REMAINING_SECONDS),
    )
}

@ComposePreview(
    showBackground = true,
    backgroundColor = CHALLA_PREVIEW_BACKGROUND,
    widthDp = 390,
    name = "Gallery - 인화 완료",
)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryContentPrintedPreview() {
    GalleryContentPreviewTemplate(photoInfo = PhotoInfo.Printed(previewGalleryPhotos()))
}

@ComposePreview(showBackground = true, backgroundColor = CHALLA_PREVIEW_BACKGROUND, name = "Gallery - Empty")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryContentEmptyPreview() {
    GalleryContentPreviewTemplate(photoInfo = PhotoInfo.Empty)
}

@ComposePreview(showBackground = true, backgroundColor = CHALLA_PREVIEW_BACKGROUND, name = "Gallery - Loading")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryContentLoadingPreview() {
    GalleryContentPreviewTemplate(photoInfo = PhotoInfo.Loading)
}

@ComposePreview(showBackground = true, backgroundColor = CHALLA_PREVIEW_BACKGROUND, name = "Gallery - Error")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryContentErrorPreview() {
    GalleryContentPreviewTemplate(photoInfo = PhotoInfo.Error)
}

@Composable
private fun GalleryContentPreviewTemplate(photoInfo: PhotoInfo) {
    GalleryContent(
        modifier = Modifier.fillMaxSize(),
        state =
            GalleryState(
                roomName = "친구들과 강릉 여행",
                members = previewGalleryMembers(),
                photoInfo = photoInfo,
            ),
        onIntent = {},
    )
}
