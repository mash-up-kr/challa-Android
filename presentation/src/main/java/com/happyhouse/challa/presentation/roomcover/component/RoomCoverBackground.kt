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
import com.happyhouse.challa.presentation.roomcover.model.RoomCoverUiModel

/**
 * 방 커버 그림. 홈의 촬영 중 카드와 커버 수정 화면 미리보기가 함께 쓴다.
 *
 * 검정 바탕 위에 배경 이미지를 깔고, 그 위에 스티커를 선택한 색으로 물들여 얹는다.
 */
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
                        imageUrl = "https://challa.example/sticker.png",
                        color = Color(0xFFD5F700),
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
        cover = RoomCoverUiModel(imageUrl = "https://challa.example/cover.jpg"),
        modifier = Modifier.size(width = 200.dp, height = 266.dp),
    )
}
