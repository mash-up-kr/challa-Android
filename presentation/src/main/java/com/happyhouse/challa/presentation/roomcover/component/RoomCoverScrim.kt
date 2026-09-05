package com.happyhouse.challa.presentation.roomcover.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.roomcover.PREVIEW_COVER_IMAGE_URL
import com.happyhouse.challa.presentation.roomcover.model.RoomCoverUiModel

/** 흰색 오버레이가 사라지는 지점. 시안에서 카드 높이의 78%로 측정했다. */
private const val WHITE_OVERLAY_END = 0.78f

/**
 * 커버 위에 얹는 오버레이. 커버 수정 화면의 미리보기 카드와 홈의 촬영 중 카드가 같이 쓴다.
 *
 * 커버 사진이 밝아도 방 이름이 읽히도록 위쪽을 어둡게 덮고, 그 위를 다시 살짝 띄운다.
 */
@Composable
fun RoomCoverScrim(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.Black.copy(alpha = 0.8f),
                                    Color.Black.copy(alpha = 0.2f),
                                ),
                        ),
                    ),
        )
        // 검정 위에 얹어야 시안과 같은 밝기가 나온다.
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops =
                                arrayOf(
                                    0f to Color.White.copy(alpha = 0.2f),
                                    WHITE_OVERLAY_END to Color.Transparent,
                                ),
                        ),
                    ),
        )
    }
}

@Preview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomCoverScrimPreview() {
    Box(modifier = Modifier.size(width = 200.dp, height = 266.dp)) {
        RoomCoverBackground(
            cover = RoomCoverUiModel(imageUrl = PREVIEW_COVER_IMAGE_URL),
            modifier = Modifier.fillMaxSize(),
        )
        RoomCoverScrim(modifier = Modifier.fillMaxSize())
    }
}
