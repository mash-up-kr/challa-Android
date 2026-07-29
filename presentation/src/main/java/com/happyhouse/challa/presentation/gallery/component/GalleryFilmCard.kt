package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

// 디자인 기준 82 x 109.33 필름 카드 비율
private const val FILM_CARD_ASPECT_RATIO = 82f / 109.33f

private val FilmCardShape = RoundedCornerShape(10.dp)

// TODO: 디자인 토큰에 없는 값이라 컴포넌트 로컬 상수로 둔다. 토큰 추가되면 교체할 것.
private val FilmCardEmptyColor = Color.White.copy(alpha = 0.05f)

/**
 * 인화 전 필름 슬롯 1칸
 * 아직 공개되지 않은 자리라 사진 없이 번호만 노출한다.
 */
@Composable
fun GalleryFilmCard(
    order: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .aspectRatio(FILM_CARD_ASPECT_RATIO)
                .clip(FilmCardShape)
                .background(FilmCardEmptyColor)
                .border(
                    width = 1.dp,
                    color = ChallaTheme.colors.lineNeutral,
                    shape = FilmCardShape,
                ),
    ) {
        Text(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 9.dp, bottom = 9.dp),
            text = order.toString(),
            color = ChallaTheme.colors.labelSubtle,
            style = ChallaTheme.typography.bodyLarge.bold,
        )
    }
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF111111)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryFilmCardPreview() {
    GalleryFilmCard(
        modifier = Modifier.width(82.dp),
        order = 1,
    )
}
