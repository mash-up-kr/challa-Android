package com.happyhouse.challa.presentation.designsystem.component.snackbar

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ChallaSnackbarContent {
    val heading: String?
    val description: String?
    val message: String

    data class HeadingOnly(
        override val heading: String,
    ) : ChallaSnackbarContent {
        override val description: String? = null
        override val message: String = heading
    }

    data class DescriptionOnly(
        override val description: String,
    ) : ChallaSnackbarContent {
        override val heading: String? = null
        override val message: String = description
    }

    data class HeadingAndDescription(
        override val heading: String,
        override val description: String,
    ) : ChallaSnackbarContent {
        override val message: String = heading
    }
}
