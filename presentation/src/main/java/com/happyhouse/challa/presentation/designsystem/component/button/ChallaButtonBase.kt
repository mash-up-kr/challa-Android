package com.happyhouse.challa.presentation.designsystem.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce

@Composable
internal fun ChallaButtonBase(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ChallaButtonVariant = ChallaButtonVariant.PRIMARY,
    minHeight: Dp,
    contentPadding: PaddingValues,
    content: @Composable (contentColor: Color) -> Unit,
) {
    val colorSpec = variant.colorSpec(enabled)

    Box(
        modifier =
            modifier
                .heightIn(min = minHeight)
                .clip(shape = RoundedCornerShape(12.dp))
                .background(colorSpec.containerColor)
                .noRippleClickOnce(
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        content(colorSpec.contentColor)
    }
}
