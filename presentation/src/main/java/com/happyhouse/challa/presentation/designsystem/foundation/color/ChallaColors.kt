package com.happyhouse.challa.presentation.designsystem.foundation.color

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class ChallaColors(
    /** Primary 영역에서 사용자가 선택한 테마에 따라 바뀌는 semantic color입니다. */
    val primary: Color,
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
    // Status
    val statusPositive: Color,
    val statusCautionary: Color,
    val statusDestructive: Color,
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
