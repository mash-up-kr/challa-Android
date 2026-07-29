package com.happyhouse.challa.presentation.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.gallery.component.GalleryBackgroundGlow
import com.happyhouse.challa.presentation.gallery.component.GalleryBottomBar
import com.happyhouse.challa.presentation.gallery.component.GalleryBottomBarEstimatedHeight
import com.happyhouse.challa.presentation.gallery.component.GalleryContent
import com.happyhouse.challa.presentation.gallery.component.GalleryTopBar
import com.happyhouse.challa.presentation.gallery.contract.GalleryIntent
import com.happyhouse.challa.presentation.gallery.contract.GalleryState
import com.happyhouse.challa.presentation.gallery.contract.GalleryState.PhotoInfo
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private val SnackbarTopPadding = 8.dp

@Composable
fun GalleryScreen(
    state: GalleryState,
    snackbarHostState: SnackbarHostState,
    onIntent: (GalleryIntent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.background(ChallaTheme.colors.backgroundSurface)) {
        GalleryBackgroundGlow(modifier = Modifier.align(Alignment.BottomCenter))

        GalleryScaffold(
            state = state,
            snackbarHostState = snackbarHostState,
            onIntent = onIntent,
            onBackClick = onBackClick,
        )
    }
}

@Composable
private fun GalleryScaffold(
    state: GalleryState,
    snackbarHostState: SnackbarHostState,
    onIntent: (GalleryIntent) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            GalleryTopBar(
                title = state.roomName,
                onBackClick = onBackClick,
            )
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            // 하단 바가 그리드 위에 떠 있으므로, 끝까지 스크롤했을 때 마지막 줄이 가리지 않도록
            // 실제로 차지하는 높이만큼 그리드 아래 여백을 준다.
            // 측정 전 첫 프레임에 여백이 튀지 않도록 예상 높이로 시작한다.
            var bottomBarHeight by remember { mutableStateOf(GalleryBottomBarEstimatedHeight) }
            val density = LocalDensity.current

            GalleryContent(
                modifier = Modifier.fillMaxSize(),
                state = state,
                onIntent = onIntent,
                extraBottomPadding = bottomBarHeight,
            )

            // 디자인상 하단 바는 그리드를 밀지 않고 위에 떠 있다.
            val photoInfo = state.photoInfo
            if (photoInfo is PhotoInfo.Waiting) {
                GalleryBottomBar(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .onSizeChanged { size ->
                                bottomBarHeight = with(density) { size.height.toDp() }
                            },
                    remainingSeconds = photoInfo.remainingSeconds,
                    onCountdownClick = { onIntent(GalleryIntent.PrintCountdownClick) },
                )
            }

            // TODO: #53에서 들어오는 ChallaSnackbarHost + ChallaToastVisuals로 교체할 것.
            SnackbarHost(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = SnackbarTopPadding),
                hostState = snackbarHostState,
            )
        }
    }
}

@ComposePreview(showBackground = true, widthDp = 390, heightDp = 844, name = "GalleryScreen - 인화 전")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryScreenWaitingPreview() {
    GalleryScreenPreviewTemplate(
        photoInfo = PhotoInfo.Waiting(slots = previewGalleryFilmSlots(), remainingSeconds = PREVIEW_REMAINING_SECONDS),
    )
}

@ComposePreview(showBackground = true, widthDp = 390, heightDp = 844, name = "GalleryScreen - 인화 완료")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryScreenPrintedPreview() {
    GalleryScreenPreviewTemplate(photoInfo = PhotoInfo.Printed(previewGalleryPhotos()))
}

@ComposePreview(showBackground = true, widthDp = 390, heightDp = 844, name = "GalleryScreen - Loading")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryScreenLoadingPreview() {
    GalleryScreenPreviewTemplate(photoInfo = PhotoInfo.Loading)
}

@ComposePreview(showBackground = true, widthDp = 390, heightDp = 844, name = "GalleryScreen - Error")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryScreenErrorPreview() {
    GalleryScreenPreviewTemplate(photoInfo = PhotoInfo.Error)
}

@ComposePreview(showBackground = true, widthDp = 390, heightDp = 844, name = "GalleryScreen - Empty")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryScreenEmptyPreview() {
    GalleryScreenPreviewTemplate(photoInfo = PhotoInfo.Empty)
}

@Composable
private fun GalleryScreenPreviewTemplate(photoInfo: PhotoInfo) {
    GalleryScreen(
        state =
            GalleryState(
                roomName = "친구들과 강릉 여행",
                members = previewGalleryMembers(),
                photoInfo = photoInfo,
            ),
        snackbarHostState = remember { SnackbarHostState() },
        onIntent = {},
        onBackClick = {},
    )
}
