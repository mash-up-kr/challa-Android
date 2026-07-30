package com.happyhouse.challa.presentation.designsystem.component.snackbar

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.foundation.icon.ChallaIconSize
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce

@Composable
fun ChallaSnackbar(
    content: ChallaSnackbarContent,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    iconTint: Color? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    onCloseClick: (() -> Unit)? = null,
) {
    ChallaMessageContent(
        heading = content.heading,
        description = content.description,
        modifier = modifier.fillMaxWidth(),
        icon = icon,
        iconTint = iconTint,
        actionLabel = actionLabel,
        onActionClick = onActionClick,
        onCloseClick = onCloseClick,
        fillTextWidth = true,
    )
}

@Composable
fun ChallaToast(
    heading: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    iconTint: Color? = null,
) {
    ChallaMessageContent(
        heading = heading,
        description = null,
        modifier = modifier.wrapContentWidth(),
        icon = icon,
        iconTint = iconTint,
        actionLabel = null,
        onActionClick = null,
        onCloseClick = null,
        fillTextWidth = false,
    )
}

@Composable
private fun ChallaMessageContent(
    heading: String?,
    description: String?,
    modifier: Modifier,
    @DrawableRes icon: Int?,
    iconTint: Color?,
    actionLabel: String?,
    onActionClick: (() -> Unit)?,
    onCloseClick: (() -> Unit)?,
    fillTextWidth: Boolean,
) {
    Row(
        modifier =
            modifier
                .heightIn(min = if (heading != null && description != null) 65.dp else 50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ChallaTheme.colors.backgroundLevel1.copy(alpha = 0.77f))
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(
                painter = painterResource(it),
                contentDescription = null,
                modifier = Modifier.size(ChallaIconSize.V22.dp),
                tint = iconTint ?: ChallaTheme.colors.labelSubtle,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier =
                Modifier
                    .weight(
                        weight = 1f,
                        fill = fillTextWidth,
                    )
                    .padding(vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            heading?.let {
                Text(
                    text = it,
                    color = ChallaTheme.colors.labelNormal,
                    overflow = TextOverflow.Ellipsis,
                    style = ChallaTheme.typography.bodySmall.medium,
                )
            }

            description?.let {
                Text(
                    text = it,
                    color = ChallaTheme.colors.labelSubtle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = ChallaTheme.typography.bodyXSmall.regular,
                )
            }
        }

        actionLabel?.let {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = it,
                modifier =
                    Modifier
                        .widthIn(min = 57.dp)
                        .heightIn(min = 32.dp)
                        .wrapContentHeight(Alignment.CenterVertically)
                        .snackbarActionClick(onActionClick),
                color = ChallaTheme.colors.labelNormal,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                style = ChallaTheme.typography.bodyXSmall.bold,
            )
        }

        onCloseClick?.let { close ->
            Box(
                modifier =
                    Modifier
                        .size(24.dp)
                        .noRippleClickOnce(
                            role = Role.Button,
                            onClick = close,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(ChallaIcons.Close),
                    contentDescription = "닫기",
                    modifier = Modifier.size(ChallaIconSize.V20.dp),
                    tint = ChallaTheme.colors.labelSubtle,
                )
            }
        }
    }
}

private fun Modifier.snackbarActionClick(onClick: (() -> Unit)?): Modifier =
    onClick?.let {
        noRippleClickOnce(
            role = Role.Button,
            onClick = it,
        )
    } ?: this

@Preview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaSnackbarPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ChallaSnackbar(
            content =
                ChallaSnackbarContent.HeadingAndDescription(
                    heading = "마침표를 붙이지 않아요",
                    description = "설명은 필요할 때만 써요",
                ),
            actionLabel = "텍스트",
            onActionClick = {},
        )
        ChallaSnackbar(
            content =
                ChallaSnackbarContent.DescriptionOnly(
                    description = "메시지가 두 줄 이상 길어지는 경우 예외적으로 사용해요",
                ),
            actionLabel = "텍스트",
            onActionClick = {},
        )
        ChallaSnackbar(
            content =
                ChallaSnackbarContent.HeadingOnly(
                    heading = "마침표를 붙이지 않아요",
                ),
            icon = ChallaIcons.Blank,
            actionLabel = "텍스트",
            onActionClick = {},
        )
        ChallaSnackbar(
            content =
                ChallaSnackbarContent.HeadingAndDescription(
                    heading = "마침표를 붙이지 않아요",
                    description = "설명은 필요할 때만 써요",
                ),
            icon = ChallaIcons.Blank,
            actionLabel = "텍스트",
            onActionClick = {},
        )
        ChallaSnackbar(
            content =
                ChallaSnackbarContent.HeadingOnly(
                    heading = "마침표를 붙이지 않아요",
                ),
            actionLabel = "텍스트",
            onCloseClick = {},
        )
    }
}

@Preview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaToastPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ChallaToast(
            heading = "마침표를 붙이지 않아요",
        )
        ChallaToast(
            heading = "마침표를 붙이지 않아요",
            icon = ChallaIcons.Error,
            iconTint = ChallaTheme.colors.statusDestructive,
        )
    }
}
