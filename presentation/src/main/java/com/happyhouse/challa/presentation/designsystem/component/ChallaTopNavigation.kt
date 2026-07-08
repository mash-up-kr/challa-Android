package com.happyhouse.challa.presentation.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonVariant
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaIconButton
import com.happyhouse.challa.presentation.designsystem.foundation.typography.DirtylineFamily
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewItem
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

enum class ChallaTopNavigationVariant {
    MAIN,
    SUB,
}

@Composable
fun ChallaTopNavigation(
    title: String,
    modifier: Modifier = Modifier,
    variant: ChallaTopNavigationVariant = ChallaTopNavigationVariant.MAIN,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    when (variant) {
        ChallaTopNavigationVariant.MAIN ->
            ChallaMainTopNavigation(
                title = title,
                modifier = modifier,
                trailingIcon = trailingIcon,
            )

        ChallaTopNavigationVariant.SUB ->
            ChallaSubTopNavigation(
                title = title,
                modifier = modifier,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
            )
    }
}

@Composable
private fun ChallaMainTopNavigation(
    title: String,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = ChallaTheme.colors.primaryYellow,
            style =
                TextStyle(
                    fontFamily = DirtylineFamily,
                    fontSize = 36.sp,
                ),
        )

        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(16.dp))
        }

        trailingIcon?.invoke()
    }
}

@Composable
private fun ChallaSubTopNavigation(
    title: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 8.dp),
    ) {
        Box(
            modifier = Modifier.align(Alignment.CenterStart),
            contentAlignment = Alignment.CenterStart,
        ) {
            leadingIcon?.invoke()
        }

        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            color = ChallaTheme.colors.labelNormal,
            textAlign = TextAlign.Center,
            style = ChallaTheme.typography.bodyLarge,
        )

        Box(
            modifier = Modifier.align(Alignment.CenterEnd),
            contentAlignment = Alignment.CenterEnd,
        ) {
            trailingIcon?.invoke()
        }
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaMainTopNavigationPreview() {
    Column(modifier = Modifier.width(390.dp)) {
        ChallaPreviewItem(label = "variant = MAIN, trailingIcon = false") {
            ChallaTopNavigation(
                title = "home",
                variant = ChallaTopNavigationVariant.MAIN,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        ChallaPreviewItem(label = "variant = MAIN, trailingIcon = true") {
            ChallaTopNavigation(
                title = "home",
                variant = ChallaTopNavigationVariant.MAIN,
                trailingIcon = {
                    ChallaIconButton(
                        icon = ChallaIcons.Blank,
                        onClick = {},
                        variant = ChallaButtonVariant.TRANSPARENT,
                    )
                },
            )
        }
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaSubTopNavigationPreview() {
    Column(modifier = Modifier.width(390.dp)) {
        ChallaSubTopNavigationPreviewItem(
            leadingIcon = false,
            trailingIcon = false,
        )
        Spacer(modifier = Modifier.height(12.dp))

        ChallaSubTopNavigationPreviewItem(
            leadingIcon = true,
            trailingIcon = false,
        )
        Spacer(modifier = Modifier.height(12.dp))

        ChallaSubTopNavigationPreviewItem(
            leadingIcon = false,
            trailingIcon = true,
        )
        Spacer(modifier = Modifier.height(12.dp))

        ChallaSubTopNavigationPreviewItem(
            leadingIcon = true,
            trailingIcon = true,
        )
    }
}

@Composable
private fun ChallaSubTopNavigationPreviewItem(
    leadingIcon: Boolean,
    trailingIcon: Boolean,
) {
    ChallaPreviewItem(
        label = "variant = SUB, leadingIcon = $leadingIcon, trailingIcon = $trailingIcon",
    ) {
        ChallaTopNavigation(
            title = "타이틀",
            variant = ChallaTopNavigationVariant.SUB,
            leadingIcon =
                if (leadingIcon) {
                    {
                        ChallaIconButton(
                            icon = ChallaIcons.Blank,
                            onClick = {},
                            variant = ChallaButtonVariant.TRANSPARENT,
                        )
                    }
                } else {
                    null
                },
            trailingIcon =
                if (trailingIcon) {
                    {
                        ChallaIconButton(
                            icon = ChallaIcons.Blank,
                            onClick = {},
                            variant = ChallaButtonVariant.TRANSPARENT,
                        )
                    }
                } else {
                    null
                },
        )
    }
}
