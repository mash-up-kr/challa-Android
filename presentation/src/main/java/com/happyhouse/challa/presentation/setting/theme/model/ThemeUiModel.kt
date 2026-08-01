package com.happyhouse.challa.presentation.setting.theme.model

import com.happyhouse.challa.domain.model.PrimaryTheme

enum class ThemeUiModel {
    LEMONADE,
    RASPBERRY,
    ORANGE,
    CIDER,
    BLUEBERRY,
    ACAI_BOWL,
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
