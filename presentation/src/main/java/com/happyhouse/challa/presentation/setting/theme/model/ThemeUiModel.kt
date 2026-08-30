package com.happyhouse.challa.presentation.setting.theme.model

import androidx.annotation.StringRes
import com.happyhouse.challa.domain.model.PrimaryTheme
import com.happyhouse.challa.presentation.R

enum class ThemeUiModel(
    @param:StringRes val titleRes: Int,
) {
    LEMONADE(R.string.theme_lemonade),
    RASPBERRY(R.string.theme_raspberry),
    ORANGE(R.string.theme_orange),
    CIDER(R.string.theme_cider),
    BLUEBERRY(R.string.theme_blueberry),
    ACAI_BOWL(R.string.theme_acai_bowl),
}

internal fun PrimaryTheme.toUiModel(): ThemeUiModel =
    when (this) {
        PrimaryTheme.LEMONADE -> ThemeUiModel.LEMONADE
        PrimaryTheme.RASPBERRY -> ThemeUiModel.RASPBERRY
        PrimaryTheme.ORANGE -> ThemeUiModel.ORANGE
        PrimaryTheme.CIDER -> ThemeUiModel.CIDER
        PrimaryTheme.BLUEBERRY -> ThemeUiModel.BLUEBERRY
        PrimaryTheme.ACAI_BOWL -> ThemeUiModel.ACAI_BOWL
    }

internal fun ThemeUiModel.toDomainModel(): PrimaryTheme =
    when (this) {
        ThemeUiModel.LEMONADE -> PrimaryTheme.LEMONADE
        ThemeUiModel.RASPBERRY -> PrimaryTheme.RASPBERRY
        ThemeUiModel.ORANGE -> PrimaryTheme.ORANGE
        ThemeUiModel.CIDER -> PrimaryTheme.CIDER
        ThemeUiModel.BLUEBERRY -> PrimaryTheme.BLUEBERRY
        ThemeUiModel.ACAI_BOWL -> PrimaryTheme.ACAI_BOWL
    }
