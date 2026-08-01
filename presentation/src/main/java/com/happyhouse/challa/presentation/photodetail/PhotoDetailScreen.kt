package com.happyhouse.challa.presentation.photodetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarHost
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.photodetail.component.PhotoDetailContent
import com.happyhouse.challa.presentation.photodetail.component.PhotoDetailTopBar
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState.PhotoInfo
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailUiModel
import kotlinx.collections.immutable.persistentListOf
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

// TODO: 디자인 토큰에 없는 값이라 화면 로컬 상수로 둔다. 토큰 추가되면 교체할 것.
private val PhotoDetailBackgroundColor = Color(0xFF111111)

@Composable
fun PhotoDetailScreen(
    state: PhotoDetailState,
    snackbarHostState: SnackbarHostState,
    onRetryClick: () -> Unit,
    onSaveClick: (PhotoDetailUiModel) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val photos = (state.photoInfo as? PhotoInfo.Loaded)?.photos ?: persistentListOf()
    val pagerState =
        key(photos) {
            rememberPagerState(
                initialPage = photos.indexOfFirst { it.id == state.initialPhotoId }.coerceAtLeast(0),
                pageCount = { photos.size },
            )
        }

    Scaffold(
        modifier = modifier,
        containerColor = PhotoDetailBackgroundColor,
        // 시스템 바 인셋은 화면 밖에서 처리한다.
        // 하단은 ChallaNavHost의 navigationBarsPadding이, 상단은 PhotoDetailTopBar의 statusBarsPadding이 담당하므로
        // 여기서 기본 인셋을 다시 적용하지 않는다.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            PhotoDetailTopBar(
                title = state.roomName,
                onBackClick = onBackClick,
                onSaveClick =
                    photos.takeIf { it.isNotEmpty() }?.let { loadedPhotos ->
                        { onSaveClick(loadedPhotos[pagerState.currentPage]) }
                    },
                isSaveEnabled = !state.isSaving,
            )
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            PhotoDetailContent(
                modifier = Modifier.fillMaxSize(),
                state = state,
                pagerState = pagerState,
                onRetryClick = onRetryClick,
            )

            // 토스트 표시 위치(topOffset)는 SideEffect를 띄우는 Route에서 지정한다.
            ChallaSnackbarHost(hostState = snackbarHostState)
        }
    }
}

@ComposePreview(showBackground = true, widthDp = 390, heightDp = 844)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailScreenPreview() {
    PhotoDetailScreen(
        modifier = Modifier.fillMaxSize(),
        state =
            PhotoDetailState(
                roomName = "해피하우스 강릉 여행",
                initialPhotoId = 0L,
                photoInfo = PhotoInfo.Loaded(previewPhotoDetailPhotos(count = 24)),
            ),
        snackbarHostState = remember { SnackbarHostState() },
        onRetryClick = {},
        onSaveClick = {},
        onBackClick = {},
    )
}

@ComposePreview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
    name = "PhotoDetailScreen - Loading",
)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailScreenLoadingPreview() {
    PhotoDetailScreen(
        modifier = Modifier.fillMaxSize(),
        state =
            PhotoDetailState(
                roomName = "해피하우스 강릉 여행",
                photoInfo = PhotoInfo.Loading,
            ),
        snackbarHostState = remember { SnackbarHostState() },
        onRetryClick = {},
        onSaveClick = {},
        onBackClick = {},
    )
}

@ComposePreview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
    name = "PhotoDetailScreen - Error",
)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailScreenErrorPreview() {
    PhotoDetailScreen(
        modifier = Modifier.fillMaxSize(),
        state =
            PhotoDetailState(
                roomName = "해피하우스 강릉 여행",
                photoInfo = PhotoInfo.Error,
            ),
        snackbarHostState = remember { SnackbarHostState() },
        onRetryClick = {},
        onSaveClick = {},
        onBackClick = {},
    )
}

@ComposePreview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
    name = "PhotoDetailScreen - Empty",
)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailScreenEmptyPreview() {
    PhotoDetailScreen(
        modifier = Modifier.fillMaxSize(),
        state =
            PhotoDetailState(
                roomName = "해피하우스 강릉 여행",
                photoInfo = PhotoInfo.Empty,
            ),
        snackbarHostState = remember { SnackbarHostState() },
        onRetryClick = {},
        onSaveClick = {},
        onBackClick = {},
    )
}
