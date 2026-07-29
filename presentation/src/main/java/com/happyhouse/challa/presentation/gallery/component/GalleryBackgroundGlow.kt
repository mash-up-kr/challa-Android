package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp

// TODO: 디자인 토큰에 없는 값이라 로컬 상수로 둔다. 토큰 추가되면 교체할 것.
private val GlowColor = Color(0xFFEAFF00)

// 디자인 기준 777 x 594 타원이 화면 아래로 43dp 벗어난 위치에 놓인다.
// blur(σ=150)가 원본 alpha 0.2를 넓게 퍼뜨리므로, 퍼진 만큼 반지름은 키우고 alpha는 낮춘다.
private const val GLOW_RADIUS_RATIO = 1.77f
private const val GLOW_ASPECT_RATIO = 0.87f
private const val GLOW_CORE_ALPHA = 0.1f
private const val GLOW_EDGE_ALPHA = 0.05f
private const val GLOW_EDGE_STOP = 0.56f
private val GlowBottomOffset = 43.dp

/**
 * 화면 아래에서 올라오는 배경 글로우
 *
 * 디자인은 타원 + Gaussian blur지만 [Modifier.blur]가 API 31부터라
 * 같은 색·위치의 radial gradient로 대신 그린다.
 */
internal fun Modifier.galleryBackgroundGlow(): Modifier =
    drawBehind {
        val radiusX = size.width * GLOW_RADIUS_RATIO
        val center = Offset(x = size.width / 2f, y = size.height + GlowBottomOffset.toPx())

        scale(
            scaleX = 1f,
            scaleY = GLOW_ASPECT_RATIO,
            pivot = center,
        ) {
            drawCircle(
                brush =
                    Brush.radialGradient(
                        0f to GlowColor.copy(alpha = GLOW_CORE_ALPHA),
                        GLOW_EDGE_STOP to GlowColor.copy(alpha = GLOW_EDGE_ALPHA),
                        1f to Color.Transparent,
                        center = center,
                        radius = radiusX,
                    ),
                radius = radiusX,
                center = center,
            )
        }
    }
