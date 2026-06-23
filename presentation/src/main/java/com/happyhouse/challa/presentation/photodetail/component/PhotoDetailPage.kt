package com.happyhouse.challa.presentation.photodetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailUiModel
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private val PhotoPlaceholderColor = Color(0xFF1A1A1A)

@Composable
fun PhotoDetailPage(
    photo: PhotoDetailUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AsyncImage(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(PhotoPlaceholderColor),
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(photo.imageUrl)
                    .crossfade(true)
                    .build(),
            contentDescription = stringResource(R.string.photo_detail_photo_content_description),
            contentScale = ContentScale.Fit,
            placeholder = ColorPainter(PhotoPlaceholderColor),
            error = ColorPainter(PhotoPlaceholderColor),
        )

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = photo.photographer,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = photo.capturedDate,
            color = Color(0xFFAAAAAA),
            fontSize = 12.sp,
        )
    }
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF000000)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPagePreview() {
    PhotoDetailPage(
        modifier = Modifier.fillMaxSize(),
        photo =
            PhotoDetailUiModel(
                id = 1L,
                imageUrl = "",
                photographer = "이주연",
                capturedDate = "Oct 12 2026",
            ),
    )
}
