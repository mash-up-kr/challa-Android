package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.happyhouse.challa.presentation.R

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
    )
}
