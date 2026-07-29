package com.happyhouse.challa.presentation.gallery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.gallery.component.GalleryBottomBar
import com.happyhouse.challa.presentation.gallery.component.GalleryContent
import com.happyhouse.challa.presentation.gallery.component.GalleryToast
import com.happyhouse.challa.presentation.gallery.component.GalleryTopBar
import com.happyhouse.challa.presentation.gallery.contract.GalleryIntent
import com.happyhouse.challa.presentation.gallery.contract.GalleryState
import com.happyhouse.challa.presentation.gallery.contract.GalleryState.PhotoInfo
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

// TODO: 디자인 토큰에 없는 값이라 화면 로컬 상수로 둔다. 토큰 추가되면 교체할 것.
private val GalleryBackgroundColor = Color(0xFF111111)

@Composable
fun GalleryScreen(
    state: GalleryState,
    snackbarHostState: SnackbarHostState,
    onIntent: (GalleryIntent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = GalleryBackgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            GalleryTopBar(
                title = state.roomName,
                onBackClick = onBackClick,
            )
        },
        bottomBar = {
            // 인화 전에만 남은 시간 안내가 필요하다.
            val photoInfo = state.photoInfo
            if (photoInfo is PhotoInfo.Waiting) {
                GalleryBottomBar(
                    remainingSeconds = photoInfo.remainingSeconds,
                    onCountdownClick = { onIntent(GalleryIntent.PrintCountdownClick) },
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            GalleryContent(
                modifier = Modifier.fillMaxSize(),
                state = state,
                onIntent = onIntent,
            )

            SnackbarHost(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                hostState = snackbarHostState,
            ) { snackbarData ->
                GalleryToast(message = snackbarData.visuals.message)
            }
        }
    }
}

@ComposePreview(showBackground = true, widthDp = 390, heightDp = 844)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryScreenPreview() {
    GalleryScreen(
        snackbarHostState = remember { SnackbarHostState() },
        state =
            GalleryState(
                roomName = "친구들과 강릉 여행",
                members = previewGalleryMembers(),
                photoInfo = PhotoInfo.Waiting(slotCount = 24, remainingSeconds = 10_798L),
            ),
        onIntent = {},
        onBackClick = {},
    )
}
