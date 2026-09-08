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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaProgressIndicator
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.gallery.PREVIEW_FILM_SLOT_COUNT
import com.happyhouse.challa.presentation.gallery.PREVIEW_REMAINING_SECONDS
import com.happyhouse.challa.presentation.gallery.contract.GalleryFilmSlotUiModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryIntent
import com.happyhouse.challa.presentation.gallery.contract.GalleryState
import com.happyhouse.challa.presentation.gallery.contract.GalleryState.PhotoInfo
import com.happyhouse.challa.presentation.gallery.previewGalleryFilmSlots
import com.happyhouse.challa.presentation.gallery.previewGalleryPhotos
import kotlinx.coroutines.delay
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

/** 조회가 이만큼 넘게 걸려야 스피너를 띄운다. */
private const val LOADING_INDICATOR_DELAY_MS = 300L

/**
 * 갤러리 본문
 * 로딩/에러/촬영 중/인화 대기/인화 완료 분기
 *
 * @param extraBottomPadding 위에 떠 있는 하단 바에 그리드 마지막 줄이 가리지 않도록 더하는 여백
 * @param onPrintFilmStageChange 필름이 나오는 동안 true. 배출구가 프로필 바 자리를 쓴다.
 * @param onPrintAnimationComplete 인화 연출을 끝까지 봤을 때
 */
@Composable
fun GalleryContent(
    state: GalleryState,
    onIntent: (GalleryIntent) -> Unit,
    modifier: Modifier = Modifier,
    extraBottomPadding: Dp = 0.dp,
    onPrintFilmStageChange: (Boolean) -> Unit = {},
    onPrintAnimationComplete: () -> Unit = {},
) {
    Box(modifier = modifier) {
        // 인화 전/후 그리드는 서로 다른 LazyVerticalGrid라, 상태를 공유하지 않으면
        // 전환할 때 스크롤이 맨 위로 튄다. 두 그리드의 칸 수와 배치가 같아 그대로 이어진다.
        val gridState = rememberLazyGridState()

        // 인화 연출처럼 곧바로 이어지는 화면에서 스피너가 떴다 사라지며 깜빡이지 않도록,
        // 조회가 길어질 때만 띄운다. 프리뷰는 첫 프레임만 그려 지연이 끝나지 않으므로 처음부터 띄운다.
        val isInspecting = LocalInspectionMode.current
        val isLoading = state.photoInfo is PhotoInfo.Loading
        var showsLoadingIndicator by remember { mutableStateOf(isInspecting) }

        // 로딩을 벗어날 때는 값을 되돌리지 않는다. 이미 떠 있던 스피너가 크로스페이드로 사라져야 한다.
        LaunchedEffect(isLoading) {
            if (!isLoading || isInspecting) return@LaunchedEffect

            showsLoadingIndicator = false
            delay(LOADING_INDICATOR_DELAY_MS)
            showsLoadingIndicator = true
        }

        AnimatedContent(
            targetState = state.photoInfo,
            // 남은 시간이 1초마다 바뀌어도 다시 그리지 않도록 타입만 키로 쓴다.
            // 촬영 중/인화 대기는 같은 그리드라, 필름이 다 차도 크로스페이드되지 않게 묶는다.
            contentKey = { photoInfo ->
                if (photoInfo is PhotoInfo.Film) PhotoInfo.Film::class else photoInfo::class
            },
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "GalleryPhotoInfo",
        ) { photoInfo ->
            when (photoInfo) {
                PhotoInfo.Loading -> {
                    GalleryCenterBox {
                        if (showsLoadingIndicator) {
                            ChallaProgressIndicator()
                        }
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

                is PhotoInfo.Film -> {
                    val loadedPhotoCount =
                        remember(photoInfo.slots) {
                            photoInfo.slots.count { slot ->
                                (slot.state as? GalleryFilmSlotUiModel.State.Captured)?.imageUrl != null
                            }
                        }

                    GalleryFilmSlotGrid(
                        modifier = Modifier.fillMaxSize(),
                        slots = photoInfo.slots,
                        loadedPhotoCount = loadedPhotoCount,
                        onLoadMore = { onIntent(GalleryIntent.PhotosLoadMore) },
                        state = gridState,
                        extraBottomPadding = extraBottomPadding,
                    )
                }

                is PhotoInfo.Printed -> {
                    if (photoInfo.playsPrintAnimation) {
                        GalleryPrintAnimation(
                            modifier = Modifier.fillMaxSize(),
                            photos = photoInfo.photos,
                            gridState = gridState,
                            extraBottomPadding = extraBottomPadding,
                            onComplete = onPrintAnimationComplete,
                            onFilmStageChange = onPrintFilmStageChange,
                        )
                    } else {
                        GalleryPhotoGrid(
                            modifier = Modifier.fillMaxSize(),
                            photos = photoInfo.photos,
                            onPhotoClick = { photoId -> onIntent(GalleryIntent.PhotoClick(photoId)) },
                            onLoadMore = { onIntent(GalleryIntent.PhotosLoadMore) },
                            state = gridState,
                            extraBottomPadding = extraBottomPadding,
                        )
                    }
                }
            }
        }
    }
}

/** 로딩/에러 상태를 화면 중앙에 배치하는 영역 */
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
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
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
        TextButton(onClick = onAction) {
            Text(
                text = actionLabel,
                color = ChallaTheme.colors.primary,
                style = ChallaTheme.typography.bodyMedium.bold,
            )
        }
    }
}

@ComposePreview(name = "Gallery - 촬영 중")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryContentShootingPreview() {
    GalleryContentPreviewTemplate(photoInfo = PhotoInfo.Shooting(slots = previewGalleryFilmSlots()))
}

@ComposePreview(name = "Gallery - 촬영 중(일부 촬영)")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryContentShootingPartlyCapturedPreview() {
    GalleryContentPreviewTemplate(
        photoInfo = PhotoInfo.Shooting(slots = previewGalleryFilmSlots(capturedCount = 10)),
    )
}

@ComposePreview(name = "Gallery - 인화 대기")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryContentWaitingPreview() {
    GalleryContentPreviewTemplate(
        photoInfo =
            PhotoInfo.Waiting(
                slots = previewGalleryFilmSlots(capturedCount = PREVIEW_FILM_SLOT_COUNT),
                remainingSeconds = PREVIEW_REMAINING_SECONDS,
            ),
    )
}

@ComposePreview(name = "Gallery - 인화 완료")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryContentPrintedPreview() {
    GalleryContentPreviewTemplate(
        photoInfo =
            PhotoInfo.Printed(
                photos = previewGalleryPhotos(),
                playsPrintAnimation = false,
            ),
    )
}

@ComposePreview(name = "Gallery - Loading")
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun GalleryContentLoadingPreview() {
    GalleryContentPreviewTemplate(photoInfo = PhotoInfo.Loading)
}

@ComposePreview(name = "Gallery - Error")
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun GalleryContentErrorPreview() {
    GalleryContentPreviewTemplate(photoInfo = PhotoInfo.Error)
}

@Composable
private fun GalleryContentPreviewTemplate(photoInfo: PhotoInfo) {
    GalleryContent(
        modifier = Modifier.fillMaxSize(),
        state = GalleryState(roomName = "친구들과 강릉 여행", photoInfo = photoInfo),
        onIntent = {},
    )
}
