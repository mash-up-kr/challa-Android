package com.happyhouse.challa.presentation.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.happyhouse.challa.presentation.designsystem.foundation.color.ChallaColors
import com.happyhouse.challa.presentation.designsystem.foundation.typography.ChallaTypography

private val LocalChallaColors =
    staticCompositionLocalOf<ChallaColors> { error("No ChallaColors provided") }

private val LocalChallaTypography =
    staticCompositionLocalOf<ChallaTypography> { error("No ChallaTypography provided") }

object ChallaTheme {
    val colors: ChallaColors
        @Composable
        @ReadOnlyComposable
        get() = LocalChallaColors.current

    val typography: ChallaTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalChallaTypography.current
}

@Composable
fun ChallaTheme(
    // Challa는 현재 다크 테마만 지원하는 디자인 시스템이므로 기본값을 true로 유지합니다.
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (isDarkTheme) ChallaColorScheme.Dark else ChallaColorScheme.Light
    val typographyScheme = ChallaTypographyScheme.default()

    CompositionLocalProvider(
        LocalChallaColors provides colorScheme,
        LocalChallaTypography provides typographyScheme,
        content = content,
    )
}
