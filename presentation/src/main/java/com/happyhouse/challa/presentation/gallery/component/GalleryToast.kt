package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

// TODO: 디자인 시스템에 toast 컴포넌트가 들어오면 그쪽으로 옮길 것.
private val ToastShape = RoundedCornerShape(12.dp)

/**
 * 갤러리 화면 상단 안내 토스트
 */
@Composable
fun GalleryToast(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .heightIn(min = 50.dp)
                .clip(ToastShape)
                .background(ChallaTheme.colors.backgroundLevel3)
                .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = ChallaTheme.colors.labelNormal,
            style = ChallaTheme.typography.bodySmall.medium,
            textAlign = TextAlign.Center,
        )
    }
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF111111)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryToastPreview() {
    GalleryToast(message = "인화 대기 중이에요! 조금만 기다려주세요")
}
