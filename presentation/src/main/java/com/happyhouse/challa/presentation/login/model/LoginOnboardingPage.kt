package com.happyhouse.challa.presentation.login.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.happyhouse.challa.presentation.R

/**
 * 로그인 화면에서 좌우로 넘겨 보는 온보딩 페이지.
 *
 * 선언 순서가 곧 노출 순서다.
 */
enum class LoginOnboardingPage(
    @DrawableRes val imageRes: Int,
    @StringRes val titleRes: Int,
) {
    SHARED_CAMERA(
        imageRes = R.drawable.img_onboarding_1,
        titleRes = R.string.login_onboarding_shared_camera,
    ),
    RECORD_TOGETHER(
        imageRes = R.drawable.img_onboarding_2,
        titleRes = R.string.login_onboarding_record_together,
    ),
    SHOOT_TOGETHER(
        imageRes = R.drawable.img_onboarding_3,
        titleRes = R.string.login_onboarding_shoot_together,
    ),
    DEVELOP_IN_THREE_HOURS(
        imageRes = R.drawable.img_onboarding_4,
        titleRes = R.string.login_onboarding_develop_in_three_hours,
    ),
    REACT_AND_COMMENT(
        imageRes = R.drawable.img_onboarding_5,
        titleRes = R.string.login_onboarding_react_and_comment,
    ),
}
