package com.happyhouse.challa.presentation.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonSize
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonVariant
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaIconButton
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce

@Composable
fun ChallaList(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes leadingIcon: Int? = null,
    trailingText: String? = null,
    @DrawableRes trailingIcon: Int? = ChallaIcons.Right,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .noRippleClickOnce(
                    role = Role.Button,
                    onClick = onClick,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        leadingIcon?.let { icon ->
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = ChallaTheme.colors.labelNeutral,
            )
        }

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = ChallaTheme.colors.labelSubtle,
            overflow = TextOverflow.Ellipsis,
            style = ChallaTheme.typography.bodyMedium,
        )

        if (trailingText != null || trailingIcon != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                trailingText?.let { text ->
                    Text(
                        text = text,
                        color = ChallaTheme.colors.primaryYellow,
                        overflow = TextOverflow.Ellipsis,
                        style = ChallaTheme.typography.bodyMedium,
                    )
                }
                trailingIcon?.let { icon ->
                    ChallaIconButton(
                        icon = icon,
                        onClick = onClick,
                        contentDescription = null,
                        variant = ChallaButtonVariant.TRANSPARENT,
                        size = ChallaButtonSize.SMALL,
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 320)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaListPreview() {
    androidx.compose.foundation.layout.Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ChallaList(
            text = "텍스트",
            onClick = {},
            leadingIcon = ChallaIcons.Blank,
        )
        ChallaList(
            text = "텍스트",
            onClick = {},
        )
        ChallaList(
            text = "텍스트",
            onClick = {},
            leadingIcon = ChallaIcons.Blank,
            trailingText = "텍스트",
        )
        ChallaList(
            text = "텍스트",
            onClick = {},
            trailingText = "텍스트",
        )
        ChallaList(
            text = "텍스트",
            onClick = {},
            leadingIcon = ChallaIcons.Blank,
            trailingIcon = ChallaIcons.Check,
        )
        ChallaList(
            text = "텍스트",
            onClick = {},
            trailingIcon = ChallaIcons.Check,
        )
    }
}
