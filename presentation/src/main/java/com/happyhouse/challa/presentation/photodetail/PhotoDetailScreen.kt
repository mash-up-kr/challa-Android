package com.happyhouse.challa.presentation.photodetail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.photodetail.component.PhotoDetailContent
import com.happyhouse.challa.presentation.photodetail.component.PhotoDetailTopBar
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailIntent
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState.PhotoInfo
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
fun PhotoDetailScreen(
    state: PhotoDetailState,
    onIntent: (PhotoDetailIntent) -> Unit,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Black,
        topBar = {
            PhotoDetailTopBar(
                title = state.roomName,
                onBackClick = onBackClick,
                onMoreClick = onMoreClick,
            )
        },
    ) { innerPadding ->
        PhotoDetailContent(
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
private fun PhotoDetailScreenPreview() {
    PhotoDetailScreen(
        state =
            PhotoDetailState(
                roomName = "길고양이를찍으러가자",
                initialPhotoId = 0L,
                photoInfo = PhotoInfo.Loaded(previewPhotoDetailPhotos()),
            ),
        onIntent = {},
        onBackClick = {},
        onMoreClick = {},
    )
}
