package com.happyhouse.challa.presentation.designsystem.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal fun Modifier.dashedRoundedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp = 2.dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 6.dp,
): Modifier =
    drawWithCache {
        val strokeWidthPx = strokeWidth.toPx()
        val strokeInset = strokeWidthPx / 2
        val pathEffect =
            PathEffect.dashPathEffect(
                intervals = floatArrayOf(dashLength.toPx(), gapLength.toPx()),
            )

        onDrawBehind {
            drawRoundRect(
                color = color,
                topLeft = Offset(strokeInset, strokeInset),
                size = Size(size.width - strokeWidthPx, size.height - strokeWidthPx),
                cornerRadius = CornerRadius(cornerRadius.toPx() - strokeInset),
                style =
                    Stroke(
                        width = strokeWidthPx,
                        pathEffect = pathEffect,
                    ),
            )
        }
    }
