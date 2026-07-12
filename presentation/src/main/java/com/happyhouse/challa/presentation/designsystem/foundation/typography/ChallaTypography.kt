package com.happyhouse.challa.presentation.designsystem.foundation.typography

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle

@Immutable
data class ChallaTypography(
    // Family
    val family1: ChallaTextStyleSet,
    val family2: ChallaTextStyleSet,
    // Heading
    val headingHome: ChallaTextStyleSet,
    val headingXLarge: ChallaTextStyleSet,
    val headingLarge: ChallaTextStyleSet,
    val headingMedium: ChallaTextStyleSet,
    val headingSmall: ChallaTextStyleSet,
    val headingXSmall: ChallaTextStyleSet,
    // Body
    val bodyLarge: ChallaTextStyleSet,
    val bodyMedium: ChallaTextStyleSet,
    val bodySmall: ChallaTextStyleSet,
    val bodyXSmall: ChallaTextStyleSet,
    // Description
    val descriptionLarge: ChallaTextStyleSet,
    val descriptionMedium: ChallaTextStyleSet,
    val descriptionSmall: ChallaTextStyleSet,
)

@Immutable
data class ChallaTextStyleSet(
    val bold: TextStyle,
    val medium: TextStyle,
    val regular: TextStyle,
)
