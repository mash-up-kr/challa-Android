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
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewLabel
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

@Composable
fun ChallaTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ChallaButtonVariant = ChallaButtonVariant.PRIMARY,
    size: ChallaButtonSize = ChallaButtonSize.LARGE,
    contentColor: Color? = null,
) {
    val sizeSpec = size.spec

    ChallaButtonBase(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        variant = variant,
        minHeight = sizeSpec.minHeight,
        cornerRadius = size.cornerRadius,
        contentPadding =
            PaddingValues(
                horizontal = sizeSpec.horizontalPadding,
                vertical = sizeSpec.verticalPadding,
            ),
    ) { variantContentColor ->
        Text(
            text = text,
            color = contentColor ?: variantContentColor,
            textAlign = TextAlign.Center,
            style = sizeSpec.textStyle,
        )
    }
}

private data class ChallaTextButtonSizeSpec(
    val minHeight: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val textStyle: TextStyle,
)

private val ChallaButtonSize.spec: ChallaTextButtonSizeSpec
    @Composable
    get() =
        when (this) {
            ChallaButtonSize.LARGE ->
                ChallaTextButtonSizeSpec(
                    minHeight = 54.dp,
                    horizontalPadding = 20.dp,
                    verticalPadding = 15.dp,
                    textStyle = ChallaTheme.typography.bodyLarge.bold,
                )

            ChallaButtonSize.MEDIUM ->
                ChallaTextButtonSizeSpec(
                    minHeight = 40.dp,
                    horizontalPadding = 16.dp,
                    verticalPadding = 12.dp,
                    textStyle = ChallaTheme.typography.bodySmall.bold,
                )

            ChallaButtonSize.SMALL ->
                ChallaTextButtonSizeSpec(
                    minHeight = 32.dp,
                    horizontalPadding = 10.dp,
                    verticalPadding = 8.dp,
                    textStyle = ChallaTheme.typography.bodyXSmall.bold,
                )
        }

@Preview(widthDp = 480)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaTextButtonPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ButtonPreviewHeaderRow()
        ButtonPreviewGroup(size = ChallaButtonSize.LARGE)
        ButtonPreviewGroup(size = ChallaButtonSize.MEDIUM)
        ButtonPreviewGroup(size = ChallaButtonSize.SMALL)
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
            ChallaPreviewLabel(
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
        ChallaPreviewLabel(
            text = size.name,
            style = ChallaTheme.typography.bodyLarge.bold,
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
        ChallaPreviewLabel(
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
        ChallaTextButton(
            text = "버튼명",
            onClick = {},
            enabled = enabled,
            variant = variant,
            size = size,
        )
    }
}
