package com.happyhouse.challa.presentation.photodetail

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.photodetail.component.PhotoDetailContent
import com.happyhouse.challa.presentation.photodetail.component.PhotoDetailTopBar
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailIntent
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
    onIntent: (PhotoDetailIntent) -> Unit,
    onSaveClick: (PhotoDetailUiModel) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val photos = (state.photoInfo as? PhotoInfo.Loaded)?.photos ?: persistentListOf()
    // 사진 목록이 도착한 시점에 initialPhotoId 위치에서 시작하도록 pager 상태를 다시 만든다.
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
        // Figma는 홈 인디케이터를 콘텐츠 위에 겹쳐 두므로 하단 inset을 콘텐츠에서 빼지 않는다.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            PhotoDetailTopBar(
                title = state.roomName,
                onBackClick = onBackClick,
                // currentPage는 스크롤 중 자주 바뀌므로 컴포지션이 아니라 클릭 시점에 읽는다.
                onSaveClick =
                    photos.takeIf { it.isNotEmpty() }?.let { loadedPhotos ->
                        { onSaveClick(loadedPhotos[pagerState.currentPage]) }
                    },
            )
        },
    ) { innerPadding ->
        PhotoDetailContent(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            state = state,
            pagerState = pagerState,
            onIntent = onIntent,
        )
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
        onIntent = {},
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
        onIntent = {},
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
        onIntent = {},
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
        onIntent = {},
        onSaveClick = {},
        onBackClick = {},
    )
}
