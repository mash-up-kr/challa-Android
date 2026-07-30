package com.happyhouse.challa.presentation.gallery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarHost
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.gallery.component.GalleryBackgroundGlow
import com.happyhouse.challa.presentation.gallery.component.GalleryBottomBar
import com.happyhouse.challa.presentation.gallery.component.GalleryContent
import com.happyhouse.challa.presentation.gallery.component.GalleryTopBar
import com.happyhouse.challa.presentation.gallery.contract.GalleryIntent
import com.happyhouse.challa.presentation.gallery.contract.GalleryState
import com.happyhouse.challa.presentation.gallery.contract.GalleryState.PhotoInfo
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

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
            val density = LocalDensity.current
            val waiting = state.photoInfo as? PhotoInfo.Waiting

            // 하단 바가 그리드 위에 떠 있으므로, 끝까지 스크롤했을 때 마지막 줄이 가리지 않도록
            // 실제로 차지하는 높이만큼 그리드 아래 여백을 준다.
            // 바가 뜬 적이 없으면 잰 높이도 없으므로 0에서 시작한다.
            var bottomBarHeight by remember { mutableStateOf(0.dp) }

            // 바가 사라지면 여백도 같이 줄어든다.
            // 즉시 0으로 떨어뜨리면 페이드아웃 중에 마지막 줄이 바 밑으로 파고들어서 함께 애니메이션한다.
            val extraBottomPadding by animateDpAsState(
                targetValue = if (waiting == null) 0.dp else bottomBarHeight,
                label = "GalleryExtraBottomPadding",
            )

            GalleryContent(
                modifier = Modifier.fillMaxSize(),
                state = state,
                onIntent = onIntent,
                extraBottomPadding = extraBottomPadding,
            )

            // 디자인상 하단 바는 그리드를 밀지 않고 위에 떠 있다.
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomCenter),
                visible = waiting != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                GalleryBottomBar(
                    modifier =
                        Modifier.onSizeChanged { size ->
                            bottomBarHeight = with(density) { size.height.toDp() }
                        },
                    remainingSeconds = waiting?.remainingSeconds ?: 0L,
                    onCountdownClick = { onIntent(GalleryIntent.PrintCountdownClick) },
                )
            }

            // 토스트 표시 위치(topOffset)는 SideEffect를 띄우는 Route에서 지정한다.
            ChallaSnackbarHost(hostState = snackbarHostState)
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
