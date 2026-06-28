package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.gallery.contract.GalleryPhotoUiModel
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private val ThumbnailPlaceholderColor = Color(0xFFE0E0E0)

/**
 * 썸네일 1장: 정사각 Coil 이미지
 *
 */
@Composable
fun GalleryPhotoItem(
    photo: GalleryPhotoUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .background(ThumbnailPlaceholderColor)
                .clickable(
                    role = Role.Image,
                    onClickLabel = stringResource(R.string.gallery_open_photo),
                    onClick = onClick,
                ),
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(photo.imageUrl)
                    .crossfade(true)
                    .build(),
            contentDescription = stringResource(R.string.gallery_photo_content_description, photo.order),
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(ThumbnailPlaceholderColor),
            error = ColorPainter(ThumbnailPlaceholderColor),
        )

        OrderLabel(
            modifier = Modifier.align(Alignment.BottomEnd),
            order = photo.order,
        )
    }
}

@Composable
private fun OrderLabel(
    order: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier =
            modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0x66000000)),
                    ),
                ).padding(horizontal = 6.dp, vertical = 4.dp),
        text = stringResource(R.string.gallery_photo_order, order),
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@ComposePreview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryPhotoItemPreview() {
    GalleryPhotoItem(
        modifier = Modifier.size(120.dp),
        photo = GalleryPhotoUiModel(id = 1L, order = 1, imageUrl = ""),
        onClick = {},
    )
}
