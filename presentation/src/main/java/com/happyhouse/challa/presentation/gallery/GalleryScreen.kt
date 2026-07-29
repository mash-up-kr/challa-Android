package com.happyhouse.challa.presentation.gallery

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.gallery.component.GalleryContent
import com.happyhouse.challa.presentation.gallery.component.GalleryTopBar
import com.happyhouse.challa.presentation.gallery.contract.GalleryIntent
import com.happyhouse.challa.presentation.gallery.contract.GalleryPhotoUiModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryState
import com.happyhouse.challa.presentation.gallery.contract.GalleryState.PhotoInfo
import kotlinx.collections.immutable.toPersistentList
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

// TODO: 디자인 토큰에 없는 값이라 화면 로컬 상수로 둔다. 토큰 추가되면 교체할 것.
private val GalleryBackgroundColor = Color(0xFF111111)

@Composable
fun GalleryScreen(
    state: GalleryState,
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
    ) { innerPadding ->
        GalleryContent(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            state = state,
            onIntent = onIntent,
        )
    }
}

@ComposePreview(showBackground = true, widthDp = 390, heightDp = 844)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryScreenPreview() {
    val photos =
        (0 until 24)
            .map { index ->
                GalleryPhotoUiModel(
                    id = index.toLong(),
                    order = index + 1,
                    imageUrl = "",
                )
            }.toPersistentList()

    GalleryScreen(
        state =
            GalleryState(
                roomName = "친구들과 강릉 여행",
                photoInfo = PhotoInfo.Loaded(photos),
            ),
        onIntent = {},
        onBackClick = {},
    )
}
