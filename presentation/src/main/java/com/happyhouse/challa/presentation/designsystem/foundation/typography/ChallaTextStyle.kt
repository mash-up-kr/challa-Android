package com.happyhouse.challa.presentation.designsystem.foundation.typography

import androidx.compose.ui.text.TextStyle

data class ChallaTextStyle(
    val fontWeight: ChallaFontWeight,
    val fontSize: ChallaFontSize,
    val lineHeight: ChallaFontLineHeight,
    val fontFamily: ChallaFontFamily = ChallaFontFamily.SUIT,
    val letterSpacing: ChallaFontLetterSpacing = ChallaFontLetterSpacing.NORMAL,
) {
    fun toTextStyle(): TextStyle =
        TextStyle(
            fontFamily = fontFamily.value,
            fontWeight = fontWeight.value,
            fontSize = fontSize.value,
            lineHeight = lineHeight.value,
            letterSpacing = letterSpacing.value,
        )
}
