package com.happyhouse.challa.presentation.photodetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarHost
import com.happyhouse.challa.presentation.designsystem.layout.ChallaScaffold
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.util.challaBackgroundGlow
import com.happyhouse.challa.presentation.photodetail.component.PhotoDetailBottomBar
import com.happyhouse.challa.presentation.photodetail.component.PhotoDetailContent
import com.happyhouse.challa.presentation.photodetail.component.PhotoDetailTopBar
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState.PhotoInfo
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailUiModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

// TODO: 디자인 토큰에 없는 값이라 화면 로컬 상수로 둔다. 토큰 추가되면 교체할 것.
private val PhotoDetailBackgroundColor = Color(0xFF111111)

/** 받아둔 사진의 끝에서 이만큼 남았을 때 다음 페이지를 미리 요청한다. */
private const val LOAD_MORE_PREFETCH_PAGE_COUNT = 3

@Composable
fun PhotoDetailScreen(
    state: PhotoDetailState,
    snackbarHostState: SnackbarHostState,
    onLoadMore: () -> Unit,
    onReactionsLoad: (PhotoDetailUiModel) -> Unit,
    onSaveClick: (PhotoDetailUiModel) -> Unit,
    onEmojiClick: (PhotoDetailUiModel, ReactionEmoji) -> Unit,
    onMessageChange: (String) -> Unit,
    onSendClick: (PhotoDetailUiModel) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val photos = (state.photoInfo as? PhotoInfo.Loaded)?.photos ?: persistentListOf()
    val pagerState =
        rememberPagerState(
            initialPage = state.initialPhotoIndex.coerceIn(0, (photos.size - 1).coerceAtLeast(0)),
            pageCount = { photos.size },
        )
    val currentPhoto = photos.getOrNull(pagerState.currentPage)

    val addedEmojis =
        remember(state.photoInfo, currentPhoto) {
            val loaded = state.photoInfo as? PhotoInfo.Loaded
            if (loaded == null || currentPhoto == null) persistentSetOf() else loaded.myEmojisOf(currentPhoto.id)
        }

    LaunchedEffect(currentPhoto?.id) {
        currentPhoto?.let(onReactionsLoad)
    }

    val reachedLoadMoreThreshold by remember(photos) {
        derivedStateOf { pagerState.currentPage >= photos.size - LOAD_MORE_PREFETCH_PAGE_COUNT }
    }
    LaunchedEffect(reachedLoadMoreThreshold, photos.size) {
        if (reachedLoadMoreThreshold) onLoadMore()
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(PhotoDetailBackgroundColor)
                .challaBackgroundGlow(),
    ) {
        ChallaScaffold(
            // 배경과 Glow는 바깥 Box가 그린다. 여기서 색을 채우면 Glow가 가려진다.
            containerColor = Color.Transparent,
            // 사진이 화면 끝까지 차야 해서 content에는 기본 인셋을 주지 않는다(시스템 바는 ChallaScaffold가 바에 적용).
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                PhotoDetailTopBar(
                    title = state.roomName,
                    onBackClick = onBackClick,
                    onSaveClick = currentPhoto?.let { photo -> { onSaveClick(photo) } },
                    isSaveEnabled = !state.isSaving,
                )
            },
            bottomBar = {
                if (currentPhoto != null) {
                    PhotoDetailBottomBar(
                        // navigationBarsPadding은 ChallaScaffold가 이미 적용해 인셋을 소비했다.
                        modifier = Modifier.imePadding(),
                        message = state.messageInput,
                        isMessageSendable = state.isMessageSendable,
                        addedEmojis = addedEmojis,
                        onEmojiClick = { emoji -> onEmojiClick(currentPhoto, emoji) },
                        onMessageChange = onMessageChange,
                        onSendClick = { onSendClick(currentPhoto) },
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
                PhotoDetailContent(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    pagerState = pagerState,
                )

                // ChallaScaffold의 snackbarHostState 대신 content 안에 둔다.
                // 여기 두면 Route가 지정하는 topOffset 기준점이 상단 바 아래라 기기별 상태바 높이에 흔들리지 않는다.
                ChallaSnackbarHost(hostState = snackbarHostState)
            }
        }
    }
}

@ComposePreview
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun PhotoDetailScreenPreview() {
    PhotoDetailScreen(
        modifier = Modifier.fillMaxSize(),
        state =
            PhotoDetailState(
                roomName = "해피하우스 강릉 여행",
                initialPhotoIndex = 0,
                photoInfo = PhotoInfo.Loaded(previewPhotoDetailPhotos(count = 24)),
            ),
        snackbarHostState = remember { SnackbarHostState() },
        onLoadMore = {},
        onReactionsLoad = {},
        onSaveClick = {},
        onEmojiClick = { _, _ -> },
        onMessageChange = {},
        onSendClick = {},
        onBackClick = {},
    )
}

@ComposePreview(name = "PhotoDetailScreen - Empty")
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
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
        onLoadMore = {},
        onReactionsLoad = {},
        onSaveClick = {},
        onEmojiClick = { _, _ -> },
        onMessageChange = {},
        onSendClick = {},
        onBackClick = {},
    )
}
