package com.happyhouse.challa.presentation.designsystem.foundation.typography

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.happyhouse.challa.presentation.R

val SuitFamily =
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

val DirtylineFamily =
    FontFamily(
        Font(R.font.dirtyline_regular, FontWeight.Normal),
    )
