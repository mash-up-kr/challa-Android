package com.happyhouse.challa.presentation.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalChallaColors = staticCompositionLocalOf { ChallaColorScheme.Dark }

object ChallaTheme {
    val colors: ChallaColors
        @Composable
        @ReadOnlyComposable
        get() = LocalChallaColors.current

    val typography: ChallaTypography
        get() = ChallaTypography
}

@Composable
fun ChallaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalChallaColors provides
            if (darkTheme) {
                ChallaColorScheme.Dark
            } else {
                ChallaColorScheme.Light
            },
        content = content,
    )
}
