package com.happyhouse.challa.presentation.designsystem.foundation.typography

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

enum class ChallaFontFamily(
    val value: FontFamily,
) {
    SUIT(SuitFamily),
    DIRTYLINE(DirtylineFamily),
}

enum class ChallaFontWeight(
    val value: FontWeight,
) {
    REGULAR(FontWeight.Normal),
    SEMI_BOLD(FontWeight.SemiBold),
    BOLD(FontWeight.Bold),
}

enum class ChallaFontSize(
    private val value: Dp,
) {
    V10(10.dp),
    V11(11.dp),
    V12(12.dp),
    V14(14.dp),
    V15(15.dp),
    V16(16.dp),
    V18(18.dp),
    V20(20.dp),
    V24(24.dp),
    V28(28.dp),
    V44(44.dp),
    V48(48.dp),
    V60(60.dp),
    ;

    val sp: TextUnit
        @Composable
        get() = with(LocalDensity.current) { value.toSp() }
}

enum class ChallaFontLineHeight(
    val value: Dp,
) {
    V12(12.dp),
    V13(13.dp),
    V14(14.dp),
    V16(16.dp),
    V20(20.dp),
    V24(24.dp),
    V28(28.dp),
    V32(32.dp),
    V36(36.dp),
    V48(48.dp),
    V55(55.dp),
    V60(60.dp),
    ;

    val sp: TextUnit
        @Composable
        get() = with(LocalDensity.current) { value.toSp() }
}

enum class ChallaFontLetterSpacing(
    val value: TextUnit,
) {
    TIGHT((-0.017).em),
    NORMAL(0.em),
}
