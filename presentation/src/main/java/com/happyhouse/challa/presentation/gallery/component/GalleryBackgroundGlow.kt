package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

/**
 * 화면 아래에서 올라오는 배경 글로우
 */
@Composable
fun GalleryBackgroundGlow(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier.fillMaxWidth(),
        painter = painterResource(R.drawable.img_gallery_background_glow),
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        colorFilter = ColorFilter.tint(ChallaTheme.colors.primary),
    )
}

@ComposePreview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryBackgroundGlowPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        GalleryBackgroundGlow(modifier = Modifier.align(Alignment.BottomCenter))
    }
}
