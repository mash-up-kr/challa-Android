package com.happyhouse.challa.presentation.gallery

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

@Composable
fun GalleryScreen(
    state: GalleryState,
    onIntent: (GalleryIntent) -> Unit,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            GalleryTopBar(
                title = state.roomName,
                onBackClick = onBackClick,
                onMoreClick = onMoreClick,
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

@ComposePreview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryScreenPreview() {
    val photos =
        (0 until 12)
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
                roomName = "다낭 4박5일",
                remainingDays = 3,
                photoInfo = PhotoInfo.Loaded(photos),
            ),
        onIntent = {},
        onBackClick = {},
        onMoreClick = {},
    )
}
