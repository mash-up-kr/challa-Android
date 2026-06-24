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

val Typography =
    Typography(
        bodyLarge =
            TextStyle(
                fontFamily = SuitFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
    )
