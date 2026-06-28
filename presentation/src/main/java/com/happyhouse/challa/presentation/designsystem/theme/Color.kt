package com.happyhouse.challa.presentation.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

internal object ColorTokens {
    val PrimaryPink = Color(0xFFFF1887)
    val PrimaryOrange = Color(0xFFFF4D01)
    val PrimaryYellow = Color(0xFFD5F700)
    val PrimarySky = Color(0xFF10E6D8)
    val PrimaryBlue = Color(0xFF508EFF)
    val PrimaryPurple = Color(0xFFC67AFF)

    val Gray100 = Color(0xFFF7F7F7)
    val Gray200 = Color(0xFF8D8C92)
    val Gray300 = Color(0xFF777779)
    val Gray400 = Color(0xFF56555A)
    val Gray500 = Color(0xFFD0CFD4)
    val Gray600 = Color(0xFFADAEB3)
    val Gray700 = Color(0xFF303032)
    val Gray800 = Color(0xFF242328)
    val Gray900 = Color(0xFF131315)

    val LabelNormal = Color(0xFFF7F7F8)
    val LabelStrong = Color(0xFFFFFFFF)
    val LabelNeutral = Color(0xFFAEAFB4)
    val LabelAlternative = Color(0xFF74767B)
    val LabelDisable = Color(0xFF444549)

    val BackgroundSurface = Color(0xFF1A1A1A)
    val BackgroundLevel1 = Color(0xFF1F1F1F)
    val BackgroundLevel2 = Color(0xFF242424)
    val BackgroundLevel3 = Color(0xFF2F2F2F)
    val BackgroundLevel4 = Color(0xFF3B3B3B)

    val LineNormal = Color(0xFF818181).copy(alpha = 0.22f)
    val LineNeutral = Color(0xFF7E7E7E).copy(alpha = 0.16f)
    val LineAlternative = Color(0xFF7E7E7E).copy(alpha = 0.08f)

    val StaticWhite = Color(0xFFFFFFFF)
    val StaticBlack = Color(0xFF000000)

    val MaterialDimmer = Color(0xFF171719).copy(alpha = 0.52f)
}

@Immutable
data class ChallaColors(
    val primaryPink: Color,
    val primaryOrange: Color,
    val primaryYellow: Color,
    val primarySky: Color,
    val primaryBlue: Color,
    val primaryPurple: Color,
    val labelNormal: Color,
    val labelStrong: Color,
    val labelNeutral: Color,
    val labelAlternative: Color,
    val labelDisable: Color,
    val backgroundSurface: Color,
    val backgroundLevel1: Color,
    val backgroundLevel2: Color,
    val backgroundLevel3: Color,
    val backgroundLevel4: Color,
    val lineNormal: Color,
    val lineNeutral: Color,
    val lineAlternative: Color,
    val staticWhite: Color,
    val staticBlack: Color,
    val materialDimmer: Color,
)

internal object ChallaColorScheme {
    val Dark =
        ChallaColors(
            primaryPink = ColorTokens.PrimaryPink,
            primaryOrange = ColorTokens.PrimaryOrange,
            primaryYellow = ColorTokens.PrimaryYellow,
            primarySky = ColorTokens.PrimarySky,
            primaryBlue = ColorTokens.PrimaryBlue,
            primaryPurple = ColorTokens.PrimaryPurple,
            labelNormal = ColorTokens.LabelNormal,
            labelStrong = ColorTokens.LabelStrong,
            labelNeutral = ColorTokens.LabelNeutral,
            labelAlternative = ColorTokens.LabelAlternative,
            labelDisable = ColorTokens.LabelDisable,
            backgroundSurface = ColorTokens.BackgroundSurface,
            backgroundLevel1 = ColorTokens.BackgroundLevel1,
            backgroundLevel2 = ColorTokens.BackgroundLevel2,
            backgroundLevel3 = ColorTokens.BackgroundLevel3,
            backgroundLevel4 = ColorTokens.BackgroundLevel4,
            lineNormal = ColorTokens.LineNormal,
            lineNeutral = ColorTokens.LineNeutral,
            lineAlternative = ColorTokens.LineAlternative,
            staticWhite = ColorTokens.StaticWhite,
            staticBlack = ColorTokens.StaticBlack,
            materialDimmer = ColorTokens.MaterialDimmer,
        )

    val Light =
        Dark.copy(
            labelNormal = ColorTokens.StaticBlack,
            labelStrong = ColorTokens.StaticBlack,
            backgroundSurface = ColorTokens.StaticWhite,
            backgroundLevel1 = ColorTokens.StaticWhite,
            backgroundLevel2 = ColorTokens.BackgroundLevel2,
            backgroundLevel3 = ColorTokens.BackgroundLevel3,
            backgroundLevel4 = ColorTokens.BackgroundLevel4,
        )
}

val PrimaryPink = ColorTokens.PrimaryPink
val PrimaryOrange = ColorTokens.PrimaryOrange
val PrimaryYellow = ColorTokens.PrimaryYellow
val PrimarySky = ColorTokens.PrimarySky
val PrimaryBlue = ColorTokens.PrimaryBlue
val PrimaryPurple = ColorTokens.PrimaryPurple

val LabelNormal = ColorTokens.LabelNormal
val LabelStrong = ColorTokens.LabelStrong
val LabelNeutral = ColorTokens.LabelNeutral
val LabelAlternative = ColorTokens.LabelAlternative
val LabelDisable = ColorTokens.LabelDisable

val BackgroundSurface = ColorTokens.BackgroundSurface
val BackgroundLevel1 = ColorTokens.BackgroundLevel1
val BackgroundLevel2 = ColorTokens.BackgroundLevel2
val BackgroundLevel3 = ColorTokens.BackgroundLevel3
val BackgroundLevel4 = ColorTokens.BackgroundLevel4

val LineNormal = ColorTokens.LineNormal
val LineNeutral = ColorTokens.LineNeutral
val LineAlternative = ColorTokens.LineAlternative

val StaticWhite = ColorTokens.StaticWhite
val StaticBlack = ColorTokens.StaticBlack

val MaterialDimmer = ColorTokens.MaterialDimmer
