package com.happyhouse.challa.presentation.designsystem.foundation.typography

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

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
    val value: TextUnit,
) {
    V10(10.sp),
    V11(11.sp),
    V12(12.sp),
    V14(14.sp),
    V15(15.sp),
    V16(16.sp),
    V18(18.sp),
    V20(20.sp),
    V24(24.sp),
    V28(28.sp),
    V44(44.sp),
    V48(48.sp),
    V60(60.sp),
}

enum class ChallaFontLineHeight(
    val value: TextUnit,
) {
    V12(12.sp),
    V13(13.sp),
    V14(14.sp),
    V16(16.sp),
    V20(20.sp),
    V24(24.sp),
    V28(28.sp),
    V32(32.sp),
    V36(36.sp),
    V48(48.sp),
    V55(55.sp),
    V60(60.sp),
}

enum class ChallaFontLetterSpacing(
    val value: TextUnit,
) {
    TIGHT((-0.017).em),
    NORMAL(0.em),
}
