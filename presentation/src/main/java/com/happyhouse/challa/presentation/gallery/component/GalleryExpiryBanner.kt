package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

// TODO: 디자인 확정 시 designsystem 토큰으로 교체.
private val BannerBackgroundColor = Color(0xFFFFF3E0)
private val BannerContentColor = Color(0xFFB26A00)

/**
 * 안내 배너. 시안: 좌측 정렬 + ⓘ prefix(유니코드 placeholder).
 */
@Composable
fun GalleryExpiryBanner(
    remainingDays: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier =
            modifier
                .fillMaxWidth()
                .background(BannerBackgroundColor)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        text = stringResource(R.string.gallery_auto_delete_banner, remainingDays),
        color = BannerContentColor,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Start,
    )
}

@ComposePreview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryExpiryBannerPreview() {
    GalleryExpiryBanner(remainingDays = 3)
}
