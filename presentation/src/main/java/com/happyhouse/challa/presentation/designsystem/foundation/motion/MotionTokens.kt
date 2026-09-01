package com.happyhouse.challa.presentation.designsystem.foundation.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

object MotionTokens {
    /** 디자인 시안(Figma 프로토타입)의 EASE_OUT. CSS `cubic-bezier(0, 0, 0.58, 1)` 과 같다. */
    val EaseOut: Easing = CubicBezierEasing(0f, 0f, 0.58f, 1f)

    /** 시작에서 가속하고 끝에서 감속한다. 가운데는 거의 등속이라 긴 이동에도 늘어지지 않는다. */
    val EaseInOut: Easing = CubicBezierEasing(0.45f, 0f, 0.55f, 1f)
}
