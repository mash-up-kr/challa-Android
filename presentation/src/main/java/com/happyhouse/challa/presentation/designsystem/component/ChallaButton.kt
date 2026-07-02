package com.happyhouse.challa.presentation.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce

enum class ChallaButtonVariant {
    PRIMARY,
    NEUTRAL,
}

enum class ChallaButtonSize {
    LARGE,
    MEDIUM,
    SMALL,
}

@Composable
fun ChallaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ChallaButtonVariant = ChallaButtonVariant.PRIMARY,
    size: ChallaButtonSize = ChallaButtonSize.LARGE,
) {
    val sizeSpec = size.spec
    val colorSpec = variant.colorSpec(enabled)
    val shape = RoundedCornerShape(sizeSpec.cornerRadius)

    Box(
        modifier =
            modifier
                .height(sizeSpec.height)
                .clip(shape)
                .background(colorSpec.containerColor)
                .noRippleClickOnce(
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                ).padding(
                    horizontal = sizeSpec.horizontalPadding,
                    vertical = sizeSpec.verticalPadding,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = colorSpec.contentColor,
            textAlign = TextAlign.Center,
            style = sizeSpec.textStyle,
        )
    }
}

@Composable
fun ChallaActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ChallaButtonVariant = ChallaButtonVariant.PRIMARY,
) {
    ChallaButton(
        text = text,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        variant = variant,
        size = ChallaButtonSize.LARGE,
    )
}

private data class ChallaButtonSizeSpec(
    val height: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val cornerRadius: Dp,
    val textStyle: TextStyle,
)

private data class ChallaButtonColorSpec(
    val containerColor: Color,
    val contentColor: Color,
)

private val ChallaButtonSize.spec: ChallaButtonSizeSpec
    @Composable
    get() =
        when (this) {
            ChallaButtonSize.LARGE ->
                ChallaButtonSizeSpec(
                    height = 52.dp,
                    horizontalPadding = 20.dp,
                    verticalPadding = 16.dp,
                    cornerRadius = 12.dp,
                    textStyle = ChallaTheme.typography.bodyMedium,
                )

            ChallaButtonSize.MEDIUM ->
                ChallaButtonSizeSpec(
                    height = 44.dp,
                    horizontalPadding = 14.dp,
                    verticalPadding = 14.dp,
                    cornerRadius = 12.dp,
                    textStyle = ChallaTheme.typography.bodySmall,
                )

            ChallaButtonSize.SMALL ->
                ChallaButtonSizeSpec(
                    height = 34.dp,
                    horizontalPadding = 10.dp,
                    verticalPadding = 10.dp,
                    cornerRadius = 8.dp,
                    textStyle = ChallaTheme.typography.descriptionLarge,
                )
        }

@Composable
private fun ChallaButtonVariant.colorSpec(enabled: Boolean): ChallaButtonColorSpec =
    when (this) {
        ChallaButtonVariant.PRIMARY ->
            if (enabled) {
                ChallaButtonColorSpec(
                    containerColor = ChallaTheme.colors.labelNormal,
                    contentColor = ChallaTheme.colors.labelDisable,
                )
            } else {
                ChallaButtonColorSpec(
                    containerColor = ChallaTheme.colors.backgroundLevel2,
                    contentColor = ChallaTheme.colors.labelDisable,
                )
            }

        ChallaButtonVariant.NEUTRAL ->
            if (enabled) {
                ChallaButtonColorSpec(
                    containerColor = ChallaTheme.colors.backgroundLevel3,
                    contentColor = ChallaTheme.colors.labelNormal,
                )
            } else {
                ChallaButtonColorSpec(
                    containerColor = ChallaTheme.colors.backgroundLevel2,
                    contentColor = ChallaTheme.colors.labelDisable,
                )
            }
    }

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaButtonPreview() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        ButtonPreviewColumn(variant = ChallaButtonVariant.PRIMARY)
        ButtonPreviewColumn(variant = ChallaButtonVariant.NEUTRAL)
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaActionButtonPreview() {
    Column(
        modifier = Modifier.width(360.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ChallaActionButton(
            text = "버튼명",
            onClick = {},
            variant = ChallaButtonVariant.PRIMARY,
        )
        ChallaActionButton(
            text = "버튼명",
            onClick = {},
            variant = ChallaButtonVariant.NEUTRAL,
        )
    }
}

@Composable
private fun ButtonPreviewColumn(variant: ChallaButtonVariant) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PreviewLabel(text = variant.name)
        ChallaButtonSize.entries.forEach { size ->
            PreviewButtonRow(
                label = size.name,
                variant = variant,
                size = size,
                enabled = true,
            )
            PreviewButtonRow(
                label = size.name,
                variant = variant,
                size = size,
                enabled = false,
            )
        }
    }
}

@Composable
private fun PreviewButtonRow(
    label: String,
    variant: ChallaButtonVariant,
    size: ChallaButtonSize,
    enabled: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PreviewLabel(text = label)
        ChallaButton(
            text = "버튼명",
            onClick = {},
            enabled = enabled,
            variant = variant,
            size = size,
        )
    }
}

@Composable
private fun PreviewLabel(text: String) {
    Text(
        text = text,
        color = ChallaTheme.colors.labelNormal,
        style = ChallaTheme.typography.descriptionLarge,
    )
}
