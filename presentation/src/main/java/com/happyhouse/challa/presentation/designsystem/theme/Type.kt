package com.happyhouse.challa.presentation.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.happyhouse.challa.presentation.R

val SuitFontFamily =
    FontFamily(
        Font(R.font.suit_thin, FontWeight.Thin),
        Font(R.font.suit_extra_light, FontWeight.ExtraLight),
        Font(R.font.suit_light, FontWeight.Light),
        Font(R.font.suit_regular, FontWeight.Normal),
        Font(R.font.suit_medium, FontWeight.Medium),
        Font(R.font.suit_semi_bold, FontWeight.SemiBold),
        Font(R.font.suit_bold, FontWeight.Bold),
        Font(R.font.suit_extra_bold, FontWeight.ExtraBold),
        Font(R.font.suit_heavy, FontWeight.Black),
    )

val DirtylineFontFamily =
    FontFamily(
        Font(R.font.dirtyline, FontWeight.Normal),
    )

object ChallaTypography {
    val headingXLarge =
        TextStyle(
            fontFamily = DirtylineFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 60.sp,
            lineHeight = 60.sp,
            letterSpacing = 0.sp,
        )
    val headingLarge =
        challaTextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 28,
            lineHeight = 36,
        )
    val headingMedium =
        challaTextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 24,
            lineHeight = 32,
        )
    val headingSmall =
        challaTextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 20,
            lineHeight = 28,
        )

    val bodyLarge =
        challaTextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 18,
            lineHeight = 24,
        )
    val bodyMedium =
        challaTextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 16,
            lineHeight = 20,
        )
    val bodySmall =
        challaTextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 14,
            lineHeight = 16,
        )

    val descriptionLarge =
        challaTextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 12,
            lineHeight = 14,
        )
    val descriptionMedium =
        challaTextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 11,
            lineHeight = 13,
        )
    val descriptionSmall =
        challaTextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 10,
            lineHeight = 12,
        )
}

val Typography =
    Typography(
        displayLarge = ChallaTypography.headingXLarge,
        titleLarge = ChallaTypography.headingLarge,
        titleMedium = ChallaTypography.headingMedium,
        titleSmall = ChallaTypography.headingSmall,
        bodyLarge = ChallaTypography.bodyLarge,
        bodyMedium = ChallaTypography.bodyMedium,
        bodySmall = ChallaTypography.bodySmall,
        labelLarge = ChallaTypography.descriptionLarge,
        labelMedium = ChallaTypography.descriptionMedium,
        labelSmall = ChallaTypography.descriptionSmall,
    )

private fun challaTextStyle(
    fontWeight: FontWeight,
    fontSize: Int,
    lineHeight: Int,
): TextStyle =
    TextStyle(
        fontFamily = SuitFontFamily,
        fontWeight = fontWeight,
        fontSize = fontSize.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = 0.sp,
    )
