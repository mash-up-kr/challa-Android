package com.happyhouse.challa.presentation.designsystem.foundation.typography

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle

@Immutable
data class ChallaTypography(
    // Family
    val family1: TextStyle,
    val family2: TextStyle,
    // Weight
    val weightBold: TextStyle,
    val weightMedium: TextStyle,
    val weightRegular: TextStyle,
    // Heading
    val headingHome: TextStyle,
    val headingXLarge: TextStyle,
    val headingLarge: TextStyle,
    val headingMedium: TextStyle,
    val headingSmall: TextStyle,
    val headingXSmall: TextStyle,
    // Body
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val bodyXSmall: TextStyle,
    // Description
    val descriptionLarge: TextStyle,
    val descriptionMedium: TextStyle,
    val descriptionSmall: TextStyle,
)
