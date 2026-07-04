package com.happyhouse.challa.presentation.designsystem.component.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
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

    ChallaButtonBase(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        variant = variant,
        minHeight = sizeSpec.minHeight,
        contentPadding =
            PaddingValues(
                horizontal = sizeSpec.horizontalPadding,
                vertical = sizeSpec.verticalPadding,
            ),
    ) {
        Text(
            text = text,
            color = it,
            textAlign = TextAlign.Center,
            style = sizeSpec.textStyle,
        )
    }
}

private data class ChallaButtonSizeSpec(
    val minHeight: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val textStyle: TextStyle,
)

internal data class ChallaButtonColorSpec(
    val containerColor: Color,
    val contentColor: Color,
)

private val ChallaButtonSize.spec: ChallaButtonSizeSpec
    @Composable
    get() =
        when (this) {
            ChallaButtonSize.LARGE ->
                ChallaButtonSizeSpec(
                    minHeight = 54.dp,
                    horizontalPadding = 20.dp,
                    verticalPadding = 15.dp,
                    textStyle = ChallaTheme.typography.bodyLarge,
                )

            ChallaButtonSize.MEDIUM ->
                ChallaButtonSizeSpec(
                    minHeight = 40.dp,
                    horizontalPadding = 16.dp,
                    verticalPadding = 12.dp,
                    textStyle = ChallaTheme.typography.bodySmall,
                )
        }

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

@Preview(widthDp = 480)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaButtonPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ButtonPreviewHeaderRow()
        ButtonPreviewGroup(size = ChallaButtonSize.LARGE)
        ButtonPreviewGroup(size = ChallaButtonSize.MEDIUM)
    }
}

@Composable
private fun ButtonPreviewHeaderRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(modifier = Modifier.width(72.dp))
        ChallaButtonVariant.entries.forEach { variant ->
            PreviewLabel(
                text = variant.name,
                modifier = Modifier.width(100.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ButtonPreviewGroup(size: ChallaButtonSize) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PreviewLabel(
            text = size.name,
            style = ChallaTheme.typography.bodyLarge,
        )
        ButtonPreviewStateRow(
            label = "Enabled",
            size = size,
            enabled = true,
        )
        ButtonPreviewStateRow(
            label = "Disabled",
            size = size,
            enabled = false,
        )
    }
}

@Composable
private fun ButtonPreviewStateRow(
    label: String,
    size: ChallaButtonSize,
    enabled: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        PreviewLabel(
            text = label,
            modifier = Modifier.width(72.dp),
        )
        ChallaButtonVariant.entries.forEach { variant ->
            PreviewButtonItem(
                variant = variant,
                size = size,
                enabled = enabled,
            )
        }
    }
}

@Composable
private fun PreviewButtonItem(
    variant: ChallaButtonVariant,
    size: ChallaButtonSize,
    enabled: Boolean,
) {
    Box(
        modifier = Modifier.width(100.dp),
        contentAlignment = Alignment.Center,
    ) {
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
private fun PreviewLabel(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = ChallaTheme.typography.descriptionLarge,
    textAlign: TextAlign? = null,
) {
    Text(
        modifier = modifier,
        text = text,
        color = ChallaTheme.colors.labelNormal,
        textAlign = textAlign,
        style = style,
    )
}
