package com.happyhouse.challa.presentation.designsystem.component.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

enum class ChallaButtonVariant {
    NEUTRAL,
    PRIMARY,
    TRANSPARENT,
}

enum class ChallaButtonSize {
    LARGE,
    MEDIUM,
}

internal data class ChallaButtonColorSpec(
    val containerColor: Color,
    val contentColor: Color,
)

@Composable
internal fun ChallaButtonVariant.colorSpec(enabled: Boolean): ChallaButtonColorSpec {
    if (!enabled) {
        return ChallaButtonColorSpec(
            containerColor = ChallaTheme.colors.backgroundLevel2,
            contentColor = ChallaTheme.colors.labelDisable,
        )
    }

    return when (this) {
        ChallaButtonVariant.NEUTRAL ->
            ChallaButtonColorSpec(
                containerColor = ChallaTheme.colors.backgroundLevel3,
                contentColor = ChallaTheme.colors.labelNormal,
            )

        ChallaButtonVariant.PRIMARY ->
            ChallaButtonColorSpec(
                containerColor = ChallaTheme.colors.labelNormal,
                contentColor = ChallaTheme.colors.labelDisable,
            )

        ChallaButtonVariant.TRANSPARENT ->
            ChallaButtonColorSpec(
                containerColor = Color.Transparent,
                contentColor = ChallaTheme.colors.labelNormal,
            )
    }
}
