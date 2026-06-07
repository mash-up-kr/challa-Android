package com.happyhouse.challa.presentation.model

import androidx.annotation.StringRes
import com.happyhouse.challa.presentation.R

enum class OnboardingStep(
    @param:StringRes val titleResId: Int,
    @param:StringRes val descriptionResId: Int,
) {
    STEP_1(
        titleResId = R.string.onboarding_step1_title,
        descriptionResId = R.string.onboarding_step1_description,
    ),
    STEP_2(
        titleResId = R.string.onboarding_step2_title,
        descriptionResId = R.string.onboarding_step2_description,
    ),
    STEP_3(
        titleResId = R.string.onboarding_step3_title,
        descriptionResId = R.string.onboarding_step3_description,
    ),
}
