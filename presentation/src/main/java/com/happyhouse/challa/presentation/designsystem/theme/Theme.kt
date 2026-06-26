package com.happyhouse.challa.presentation.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
    darkColorScheme(
        primary = PrimaryPink,
        background = BackgroundSurface,
        onBackground = LabelNormal,
        surface = BackgroundLevel1,
        onSurface = LabelNormal,
        outline = LineNormal,
        error = PrimaryOrange,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = PrimaryPink,
        background = StaticWhite,
        onBackground = StaticBlack,
        surface = StaticWhite,
        onSurface = StaticBlack,
        outline = LineNormal,
        error = PrimaryOrange,
    )

@Composable
fun ChallaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        if (darkTheme) {
            DarkColorScheme
        } else {
            LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
