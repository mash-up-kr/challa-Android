package com.happyhouse.challa.presentation.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

/**
 * 원형 프로필 사진
 *
 * URL이 없거나 원격 이미지가 로딩 중이거나 로드에 실패하면 기본 이미지를 표시한다.
 */
@Composable
fun ChallaProfileImage(
    profileImageUrl: String?,
    modifier: Modifier = Modifier,
) {
    val fallbackPainter = painterResource(R.drawable.img_setting_profile_placeholder)
    val imageModifier = modifier.clip(CircleShape)

    if (profileImageUrl.isNullOrBlank()) {
        Image(
            modifier = imageModifier,
            painter = fallbackPainter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    } else {
        AsyncImage(
            modifier = imageModifier,
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(profileImageUrl)
                    .crossfade(true)
                    .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = fallbackPainter,
            error = fallbackPainter,
        )
    }
}

@ComposePreview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaProfileImagePreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ChallaProfileImage(
            modifier = Modifier.size(40.dp),
            profileImageUrl = null,
        )
    }
}
