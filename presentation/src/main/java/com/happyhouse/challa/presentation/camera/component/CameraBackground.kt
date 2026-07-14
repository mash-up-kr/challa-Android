package com.happyhouse.challa.presentation.camera.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
internal fun CameraBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        CameraBackgroundLayer(modifier = Modifier.fillMaxSize())
        CameraBackgroundLayer(
            modifier =
                Modifier
                    .fillMaxSize()
                    .progressiveBottomMask()
                    .blur(
                        radius = CAMERA_BACKGROUND_MAX_BLUR,
                        edgeTreatment = BlurredEdgeTreatment.Unbounded,
                    ),
        )
    }
}

@Composable
private fun CameraBackgroundLayer(modifier: Modifier = Modifier) {
    Spacer(
        modifier =
            modifier.background(
                Brush.verticalGradient(
                    colors =
                        listOf(
                            CameraBackgroundTopColor,
                            Color.Black,
                        ),
                ),
            ),
    )
}

private fun Modifier.progressiveBottomMask(): Modifier =
    graphicsLayer {
        compositingStrategy = CompositingStrategy.Offscreen
    }.drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black)),
            blendMode = BlendMode.DstIn,
        )
    }

internal val CameraBackgroundTopColor = Color(0xFFB3B3B3)
private val CAMERA_BACKGROUND_MAX_BLUR = 10.dp
