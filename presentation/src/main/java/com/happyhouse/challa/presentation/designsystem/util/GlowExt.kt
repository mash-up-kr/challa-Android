package com.happyhouse.challa.presentation.designsystem.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

/**
 * 화면 하단에 은은하게 깔리는 Glow.
 *
 * 피그마의 blur(150) 처리된 ellipse를 대체한다.
 * [androidx.compose.ui.draw.blur]는 API 31 미만에서 동작하지 않으므로 radial gradient로 표현한다.
 */
@Composable
fun Modifier.challaBackgroundGlow(): Modifier {
    val glowColor = ChallaTheme.colors.primary
    return drawBehind {
        val center = Offset(x = size.width / 2f, y = size.height * 0.92f)
        val radius = size.width * 0.95f
        drawRect(
            brush =
                Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.20f), Color.Transparent),
                    center = center,
                    radius = radius,
                ),
        )
    }
}
