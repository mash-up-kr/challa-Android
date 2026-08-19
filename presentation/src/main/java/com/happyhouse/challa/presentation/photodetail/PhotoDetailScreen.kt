package com.happyhouse.challa.presentation.photodetail

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarHost
import com.happyhouse.challa.presentation.designsystem.layout.ChallaScaffold
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.photodetail.component.PhotoDetailBottomBar
import com.happyhouse.challa.presentation.photodetail.component.PhotoDetailContent
import com.happyhouse.challa.presentation.photodetail.component.PhotoDetailTopBar
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState.PhotoInfo
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailUiModel
import com.happyhouse.challa.presentation.photodetail.contract.ReactionEmoji
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

// TODO: 디자인 토큰에 없는 값이라 화면 로컬 상수로 둔다. 토큰 추가되면 교체할 것.
private val PhotoDetailBackgroundColor = Color(0xFF111111)

/** 받아둔 사진의 끝에서 이만큼 남았을 때 다음 페이지를 미리 요청한다. */
private const val LOAD_MORE_PREFETCH_PAGE_COUNT = 3

@Composable
fun PhotoDetailScreen(
    state: PhotoDetailState,
    snackbarHostState: SnackbarHostState,
    onRetryClick: () -> Unit,
    onLoadMore: () -> Unit,
    onSaveClick: (PhotoDetailUiModel) -> Unit,
    onEmojiClick: (PhotoDetailUiModel, ReactionEmoji) -> Unit,
    onMessageChange: (String) -> Unit,
    onSendClick: (PhotoDetailUiModel) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val photos = (state.photoInfo as? PhotoInfo.Loaded)?.photos ?: persistentListOf()
    val pagerState = rememberPagerState(pageCount = { photos.size })
    val currentPhoto = photos.getOrNull(pagerState.currentPage)

    // 사진이 처음 도착했을 때만 진입한 사진으로 옮긴다. 다음 페이지를 이어 받아도 보던 자리를 지켜야 한다.
    var hasMovedToInitialPhoto by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(photos, state.initialPhotoId) {
        if (hasMovedToInitialPhoto || photos.isEmpty()) return@LaunchedEffect

        val initialIndex = photos.indexOfFirst { photo -> photo.id == state.initialPhotoId }
        if (initialIndex >= 0) pagerState.scrollToPage(initialIndex)

        hasMovedToInitialPhoto = true
    }

    // 이미 남긴 이모지는 다시 누르면 취소되므로, 버튼이 어떤 동작인지 접근성 라벨로 알린다.
    val addedEmojis =
        remember(state.photoInfo, currentPhoto) {
            val loaded = state.photoInfo as? PhotoInfo.Loaded
            if (loaded == null || currentPhoto == null) {
                persistentSetOf()
            } else {
                loaded.reactionsOf(currentPhoto.id).map { reaction -> reaction.emoji }.toPersistentSet()
            }
        }

    val reachedLoadMoreThreshold by remember(photos) {
        derivedStateOf { pagerState.currentPage >= photos.size - LOAD_MORE_PREFETCH_PAGE_COUNT }
    }
    LaunchedEffect(reachedLoadMoreThreshold, photos.size) {
        if (reachedLoadMoreThreshold) onLoadMore()
    }

    ChallaScaffold(
        modifier = modifier,
        containerColor = PhotoDetailBackgroundColor,
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
                onRetryClick = onRetryClick,
            )

            // ChallaScaffold의 snackbarHostState 대신 content 안에 둔다.
            // 여기 두면 Route가 지정하는 topOffset 기준점이 상단 바 아래라 기기별 상태바 높이에 흔들리지 않는다.
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
        onLoadMore = {},
        onSaveClick = {},
        onEmojiClick = { _, _ -> },
        onMessageChange = {},
        onSendClick = {},
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
        onLoadMore = {},
        onSaveClick = {},
        onEmojiClick = { _, _ -> },
        onMessageChange = {},
        onSendClick = {},
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
        onLoadMore = {},
        onSaveClick = {},
        onEmojiClick = { _, _ -> },
        onMessageChange = {},
        onSendClick = {},
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
        onLoadMore = {},
        onSaveClick = {},
        onEmojiClick = { _, _ -> },
        onMessageChange = {},
        onSendClick = {},
        onBackClick = {},
    )
}
