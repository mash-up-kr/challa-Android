package com.happyhouse.challa.presentation.designsystem.foundation.color

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class ChallaColors(
    // Primary
    val primaryPink: Color,
    val primaryOrange: Color,
    val primaryYellow: Color,
    val primarySky: Color,
    val primaryBlue: Color,
    val primaryPurple: Color,
    // Label
    val labelStrong: Color,
    val labelNormal: Color,
    val labelSubtle: Color,
    val labelNeutral: Color,
    val labelAlternative: Color,
    val labelDisable: Color,
    // Background
    val backgroundSurface: Color,
    val backgroundLevel1: Color,
    val backgroundLevel2: Color,
    val backgroundLevel3: Color,
    val backgroundLevel4: Color,
    // Line
    val lineNormal: Color,
    val lineNeutral: Color,
    val lineAlternative: Color,
    // Static
    val staticWhite: Color,
    val staticBlack: Color,
    // Material
    val materialDimmer: Color,
)
