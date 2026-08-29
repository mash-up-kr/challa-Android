package com.happyhouse.challa.presentation.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

/**
 * 원형 프로필 사진
 *
 * 기본 아이콘을 깔고 그 위에 사진을 덮으므로, 사진이 없거나 로딩·실패해도 기본 아이콘이 보인다.
 *
 * @param fallbackIconTint null이면 아이콘 원본 색을 쓴다.
 */
@Composable
fun ChallaProfileImage(
    profileImageUrl: String?,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    fallbackIconTint: Color? = null,
) {
    Box(
        modifier =
            modifier
                .clip(CircleShape)
                .background(backgroundColor),
    ) {
        Icon(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.img_setting_profile_placeholder),
            contentDescription = null,
            tint = fallbackIconTint ?: Color.Unspecified,
        )

        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(profileImageUrl)
                    .crossfade(true)
                    .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}

@ComposePreview(showBackground = true, name = "ProfileImage - 기본 아이콘 / 배경 지정")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaProfileImagePreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ChallaProfileImage(
            modifier = Modifier.size(40.dp),
            profileImageUrl = null,
        )

        ChallaProfileImage(
            modifier = Modifier.size(40.dp),
            profileImageUrl = null,
            backgroundColor = ChallaTheme.colors.backgroundLevel2,
            fallbackIconTint = ChallaTheme.colors.lineNeutral,
        )
    }
}
