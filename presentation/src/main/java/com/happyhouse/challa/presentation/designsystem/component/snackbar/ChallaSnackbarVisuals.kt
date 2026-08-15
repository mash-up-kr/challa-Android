package com.happyhouse.challa.presentation.designsystem.component.snackbar

import androidx.annotation.DrawableRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

data class ChallaSnackbarVisuals(
    val content: ChallaSnackbarContent,
    @param:DrawableRes val icon: Int? = null,
    val iconTint: Color? = null,
    val topOffset: Dp? = null,
    override val actionLabel: String? = null,
    val actionLabelColor: Color? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
) : SnackbarVisuals {
    override val message: String
        get() = content.message
}
