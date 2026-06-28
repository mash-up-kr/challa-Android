package com.happyhouse.challa.presentation.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.theme.DirtylineFontFamily

@Composable
fun ChallaBrandTopBar(
    title: String,
    modifier: Modifier = Modifier,
    actionIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = ChallaTheme.colors.primaryYellow,
            style =
                TextStyle(
                    fontFamily = DirtylineFontFamily,
                    fontSize = 36.sp,
                ),
        )

        actionIcon?.invoke()
    }
}

@Composable
fun ChallaStartTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actionIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(70.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        navigationIcon?.invoke()

        if (navigationIcon != null) {
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = ChallaTheme.colors.labelNormal,
            style = ChallaTheme.typography.headingMedium,
        )

        actionIcon?.invoke()
    }
}

@Composable
fun ChallaCenterTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actionIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(70.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        navigationIcon?.invoke()

        if (navigationIcon != null) {
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = ChallaTheme.colors.labelNormal,
            textAlign = TextAlign.Center,
            style = ChallaTheme.typography.bodyLarge,
        )

        actionIcon?.invoke()
    }
}

@Composable
fun ChallaTopBarIconButton(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
    tint: Color = ChallaTheme.colors.labelNeutral,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = tint,
        )
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaTopBarPreview() {
    Column(modifier = Modifier.width(390.dp)) {
        ChallaBrandTopBar(
            title = "camera",
            actionIcon = {
                ChallaTopBarIconButton(
                    icon = ChallaIcons.Blank,
                    onClick = {},
                )
            },
        )

        ChallaStartTopBar(
            title = "설정",
            navigationIcon = {
                ChallaTopBarIconButton(
                    icon = ChallaIcons.Blank,
                    onClick = {},
                )
            },
            actionIcon = {
                ChallaTopBarIconButton(
                    icon = ChallaIcons.Blank,
                    onClick = {},
                )
            },
        )

        ChallaCenterTopBar(
            title = "타이틀",
            navigationIcon = {
                ChallaTopBarIconButton(
                    icon = ChallaIcons.Blank,
                    onClick = {},
                )
            },
            actionIcon = {
                ChallaTopBarIconButton(
                    icon = ChallaIcons.Blank,
                    onClick = {},
                )
            },
        )
    }
}
