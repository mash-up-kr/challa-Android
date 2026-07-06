package com.happyhouse.challa.presentation.designsystem.component.button

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewLabel
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

@Composable
fun ChallaIconButton(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
    variant: ChallaButtonVariant = ChallaButtonVariant.PRIMARY,
    size: ChallaButtonSize = ChallaButtonSize.LARGE,
) {
    val sizeSpec = size.iconSpec

    ChallaButtonBase(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        variant = variant,
        minHeight = sizeSpec.minHeight,
        contentPadding = PaddingValues(sizeSpec.padding),
    ) { contentColor ->
        Icon(
            painter = painterResource(id = icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(sizeSpec.iconSize),
            tint = contentColor,
        )
    }
}

private data class ChallaIconButtonSizeSpec(
    val minHeight: Dp,
    val iconSize: Dp,
    val padding: Dp,
)

private val ChallaButtonSize.iconSpec: ChallaIconButtonSizeSpec
    get() =
        when (this) {
            ChallaButtonSize.LARGE ->
                ChallaIconButtonSizeSpec(
                    minHeight = 54.dp,
                    iconSize = 24.dp,
                    padding = 15.dp,
                )

            ChallaButtonSize.MEDIUM ->
                ChallaIconButtonSizeSpec(
                    minHeight = 40.dp,
                    iconSize = 20.dp,
                    padding = 10.dp,
                )
        }

@Preview(widthDp = 480)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaIconButtonPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IconButtonPreviewHeaderRow()
        IconButtonPreviewGroup(size = ChallaButtonSize.LARGE)
        IconButtonPreviewGroup(size = ChallaButtonSize.MEDIUM)
    }
}

@Composable
private fun IconButtonPreviewHeaderRow() {
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
private fun IconButtonPreviewGroup(size: ChallaButtonSize) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ChallaPreviewLabel(
            text = size.name,
            style = ChallaTheme.typography.bodyLarge,
        )
        IconButtonPreviewStateRow(
            label = "Enabled",
            size = size,
            enabled = true,
        )
        IconButtonPreviewStateRow(
            label = "Disabled",
            size = size,
            enabled = false,
        )
    }
}

@Composable
private fun IconButtonPreviewStateRow(
    label: String,
    size: ChallaButtonSize,
    enabled: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(60.dp),
    ) {
        ChallaPreviewLabel(
            text = label,
            modifier = Modifier.width(60.dp),
        )
        ChallaButtonVariant.entries.forEach { variant ->
            PreviewIconButtonItem(
                variant = variant,
                size = size,
                enabled = enabled,
            )
        }
    }
}

@Composable
private fun PreviewIconButtonItem(
    variant: ChallaButtonVariant,
    size: ChallaButtonSize,
    enabled: Boolean,
) {
    Box(
        modifier = Modifier.width(60.dp),
        contentAlignment = Alignment.Center,
    ) {
        ChallaIconButton(
            icon = ChallaIcons.Blank,
            onClick = {},
            enabled = enabled,
            variant = variant,
            size = size,
        )
    }
}
