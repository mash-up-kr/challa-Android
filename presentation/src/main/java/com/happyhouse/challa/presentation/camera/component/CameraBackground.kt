package com.happyhouse.challa.presentation.camera.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
internal fun CameraBackground(modifier: Modifier = Modifier) {
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

internal val CameraBackgroundTopColor = Color(0xFFB3B3B3)
