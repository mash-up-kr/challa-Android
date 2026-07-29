package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.happyhouse.challa.presentation.gallery.contract.GalleryMemberUiModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryState
import com.happyhouse.challa.presentation.gallery.contract.GalleryState.PhotoInfo
import com.happyhouse.challa.presentation.gallery.previewGalleryFilmSlots
import com.happyhouse.challa.presentation.gallery.previewGalleryMembers
import com.happyhouse.challa.presentation.gallery.previewGalleryPhotos
import kotlinx.collections.immutable.ImmutableList
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
        when (val photoInfo = state.photoInfo) {
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
                GalleryGridArea(members = state.members) {
                    GalleryFilmSlotGrid(
                        slots = photoInfo.slots,
                        extraBottomPadding = extraBottomPadding,
                    )
                }
            }

            is PhotoInfo.Printed -> {
                GalleryGridArea(members = state.members) {
                    GalleryPhotoGrid(
                        photos = photoInfo.photos,
                        onPhotoClick = { photoId -> onIntent(GalleryIntent.PhotoClick(photoId)) },
                        extraBottomPadding = extraBottomPadding,
                    )
                }
            }
        }
    }
}

/**
 * 그리드 영역
 * 참여자 프로필 바가 그리드 첫 줄 위에 겹쳐 놓인다.
 */
@Composable
private fun GalleryGridArea(
    members: ImmutableList<GalleryMemberUiModel>,
    grid: @Composable () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        grid()

        GalleryProfileBar(
            modifier = Modifier.align(Alignment.TopCenter),
            members = members,
        )
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
