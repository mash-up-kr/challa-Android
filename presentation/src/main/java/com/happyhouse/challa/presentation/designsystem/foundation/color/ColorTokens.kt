package com.happyhouse.challa.presentation.designsystem.foundation.color

import androidx.compose.ui.graphics.Color

internal object ColorTokens {
    val PrimaryPink = Color(0xFFFF1887)
    val PrimaryOrange = Color(0xFFFF4D01)
    val PrimaryYellow = Color(0xFFD5F700)
    val PrimarySky = Color(0xFF10E6D8)
    val PrimaryBlue = Color(0xFF508EFF)
    val PrimaryPurple = Color(0xFFC67AFF)

    // 보류
    val Gray100 = Color(0xFFF7F7F7)
    val Gray200 = Color(0xFF8D8C92)
    val Gray300 = Color(0xFF777779)
    val Gray400 = Color(0xFF56555A)
    val Gray500 = Color(0xFFD0CFD4)
    val Gray600 = Color(0xFFADAEB3)
    val Gray700 = Color(0xFF303032)
    val Gray800 = Color(0xFF242328)
    val Gray900 = Color(0xFF131315)

    val LabelStrong = Color(0xFFFFFFFF)
    val LabelNormal = Color(0xFFF7F7F8)
    val LabelSubtle = Color(0xFFCCCDD4)
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
