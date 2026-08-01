package com.happyhouse.challa.presentation.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.happyhouse.challa.domain.model.PrimaryTheme
import com.happyhouse.challa.presentation.designsystem.foundation.color.ChallaColors
import com.happyhouse.challa.presentation.designsystem.foundation.color.ColorTokens
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

/**
 * Challa Design System의 색상과 타이포그래피를 제공합니다.
 *
 * [primaryTheme]가 변경되면 팔레트 자체는 유지하면서 [ChallaColors.primary]만 해당 테마의
 * 강조색으로 교체됩니다.
 *
 * @param primaryTheme 앱 전반의 semantic primary color로 사용할 테마
 * @param isDarkTheme 다크 또는 라이트 color scheme 선택값
 */
@Composable
fun ChallaTheme(
    primaryTheme: PrimaryTheme = PrimaryTheme.LEMONADE,
    // Challa는 현재 다크 테마만 지원하는 디자인 시스템이므로 기본값을 true로 유지합니다.
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val baseColorScheme = if (isDarkTheme) ChallaColorScheme.Dark else ChallaColorScheme.Light
    val primaryColor =
        when (primaryTheme) {
            PrimaryTheme.LEMONADE -> ColorTokens.PrimaryYellow
            PrimaryTheme.RASPBERRY -> ColorTokens.PrimaryPink
            PrimaryTheme.ORANGE -> ColorTokens.PrimaryOrange
            PrimaryTheme.CIDER -> ColorTokens.PrimarySky
            PrimaryTheme.BLUEBERRY -> ColorTokens.PrimaryBlue
            PrimaryTheme.ACAI_BOWL -> ColorTokens.PrimaryPurple
        }
    val colorScheme =
        remember(baseColorScheme, primaryColor) {
            baseColorScheme.copy(primary = primaryColor)
        }
    val typographyScheme = ChallaTypographyScheme.Default

    CompositionLocalProvider(
        LocalChallaColors provides colorScheme,
        LocalChallaTypography provides typographyScheme,
        content = content,
    )
}
