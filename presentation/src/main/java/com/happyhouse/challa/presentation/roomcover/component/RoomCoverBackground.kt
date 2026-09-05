package com.happyhouse.challa.presentation.roomcover.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.roomcover.PREVIEW_COVER_IMAGE_URL
import com.happyhouse.challa.presentation.roomcover.PREVIEW_STICKER_IMAGE_URL
import com.happyhouse.challa.presentation.roomcover.model.RoomCoverUiModel

@Composable
fun RoomCoverBackground(
    cover: RoomCoverUiModel,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.background(Color.Black)) {
        cover.imageUrl?.let { imageUrl ->
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        cover.sticker?.let { sticker ->
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(sticker.imageUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(sticker.color),
            )
        }
    }
}

@Preview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomCoverBackgroundEmptyPreview() {
    RoomCoverBackground(
        cover = RoomCoverUiModel(),
        modifier = Modifier.size(width = 200.dp, height = 266.dp),
    )
}

@Preview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomCoverBackgroundStickerPreview() {
    RoomCoverBackground(
        cover =
            RoomCoverUiModel(
                sticker =
                    RoomCoverUiModel.Sticker(
                        imageUrl = PREVIEW_STICKER_IMAGE_URL,
                        color = ChallaTheme.colors.primaryYellow,
                    ),
            ),
        modifier = Modifier.size(width = 200.dp, height = 266.dp),
    )
}

@Preview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomCoverBackgroundImagePreview() {
    RoomCoverBackground(
        cover = RoomCoverUiModel(imageUrl = PREVIEW_COVER_IMAGE_URL),
        modifier = Modifier.size(width = 200.dp, height = 266.dp),
    )
}
