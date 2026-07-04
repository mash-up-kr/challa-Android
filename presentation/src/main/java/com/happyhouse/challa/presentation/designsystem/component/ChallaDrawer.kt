package com.happyhouse.challa.presentation.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButton
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonSize
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonVariant
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

enum class ChallaDrawerVariant {
    COMPACT,
    RICH,
}

@Composable
fun ChallaDrawer(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ChallaDrawerVariant = ChallaDrawerVariant.COMPACT,
    title: String = "",
    instanceSlot: (@Composable () -> Unit)? = null,
    primaryButton: @Composable () -> Unit,
    neutralButton: @Composable () -> Unit,
    transparentButton: @Composable () -> Unit,
) {
    ChallaDrawerContainer(
        onDismissRequest = onDismissRequest,
    ) {
        ChallaDrawerSurface(
            variant = variant,
            title = title,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            instanceSlot = instanceSlot,
            primaryButton = primaryButton,
            neutralButton = neutralButton,
            transparentButton = transparentButton,
        )
    }
}

@Composable
private fun ChallaDrawerContainer(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            content()
        }
    }
}

@Composable
private fun ChallaDrawerSurface(
    variant: ChallaDrawerVariant,
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    instanceSlot: (@Composable () -> Unit)? = null,
    primaryButton: @Composable () -> Unit,
    neutralButton: @Composable () -> Unit,
    transparentButton: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                .clip(shape = RoundedCornerShape(32.dp))
                .background(ChallaTheme.colors.backgroundLevel2)
                .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (variant) {
            ChallaDrawerVariant.COMPACT ->
                ChallaCompactDrawerContent(
                    primaryButton = primaryButton,
                    neutralButton = neutralButton,
                    transparentButton = transparentButton,
                )

            ChallaDrawerVariant.RICH ->
                ChallaRichDrawerContent(
                    title = title,
                    onDismissRequest = onDismissRequest,
                    instanceSlot = instanceSlot,
                    primaryButton = primaryButton,
                    neutralButton = neutralButton,
                    transparentButton = transparentButton,
                )
        }
    }
}

@Composable
private fun ChallaCompactDrawerContent(
    primaryButton: @Composable () -> Unit,
    neutralButton: @Composable () -> Unit,
    transparentButton: @Composable () -> Unit,
) {
    Spacer(modifier = Modifier.height(12.dp))
    ChallaDrawerHandle()
    Spacer(modifier = Modifier.height(24.dp))
    ChallaDrawerActions(
        primaryButton = primaryButton,
        neutralButton = neutralButton,
        transparentButton = transparentButton,
    )
}

@Composable
private fun ChallaRichDrawerContent(
    title: String,
    onDismissRequest: () -> Unit,
    instanceSlot: (@Composable () -> Unit)?,
    primaryButton: @Composable () -> Unit,
    neutralButton: @Composable () -> Unit,
    transparentButton: @Composable () -> Unit,
) {
    ChallaTopNavigation(
        title = title,
        variant = ChallaTopNavigationVariant.MAIN,
        trailingIcon = {
            ChallaTopNavigationIconButton(
                icon = ChallaIcons.Close,
                onClick = onDismissRequest,
                contentDescription = "닫기",
            )
        },
    )
    HorizontalDivider(color = ChallaTheme.colors.lineNeutral)
    instanceSlot?.let { slot ->
        Spacer(modifier = Modifier.height(20.dp))
        slot()
    }
    Spacer(modifier = Modifier.height(24.dp))
    ChallaDrawerActions(
        primaryButton = primaryButton,
        neutralButton = neutralButton,
        transparentButton = transparentButton,
    )
}

@Composable
private fun ChallaDrawerActions(
    primaryButton: @Composable () -> Unit,
    neutralButton: @Composable () -> Unit,
    transparentButton: @Composable () -> Unit,
) {
    primaryButton()
    Spacer(modifier = Modifier.height(8.dp))
    neutralButton()
    Spacer(modifier = Modifier.height(8.dp))
    transparentButton()
}

@Composable
private fun ChallaDrawerHandle() {
    Box(
        modifier =
            Modifier
                .size(width = 52.dp, height = 4.dp)
                .clip(shape = RoundedCornerShape(1000.dp))
                .background(Color(0xFF6C6F81)),
    )
}

@Preview(widthDp = 390, heightDp = 720)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaCompactDrawerPreview() {
    ChallaDrawerSurface(
        onDismissRequest = {},
        variant = ChallaDrawerVariant.COMPACT,
        title = "",
        primaryButton = { ChallaDrawerPreviewPrimaryButton() },
        neutralButton = { ChallaDrawerPreviewNeutralButton() },
        transparentButton = { ChallaDrawerPreviewTransparentButton() },
    )
}

@Preview(widthDp = 390, heightDp = 720)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaRichDrawerPreview() {
    ChallaDrawerSurface(
        variant = ChallaDrawerVariant.RICH,
        title = "타이틀",
        onDismissRequest = {},
        instanceSlot = {
            Text(
                text = "Instance Slot",
                color = ChallaTheme.colors.labelNormal,
                style = ChallaTheme.typography.bodySmall,
            )
        },
        primaryButton = { ChallaDrawerPreviewPrimaryButton() },
        neutralButton = { ChallaDrawerPreviewNeutralButton() },
        transparentButton = { ChallaDrawerPreviewTransparentButton() },
    )
}

@Composable
private fun ChallaDrawerPreviewPrimaryButton() {
    ChallaButton(
        text = "버튼명",
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ChallaDrawerPreviewNeutralButton() {
    ChallaButton(
        text = "버튼명",
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
        variant = ChallaButtonVariant.NEUTRAL,
    )
}

@Composable
private fun ChallaDrawerPreviewTransparentButton() {
    ChallaButton(
        text = "보조 액션",
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
        variant = ChallaButtonVariant.TRANSPARENT,
        size = ChallaButtonSize.MEDIUM,
    )
}
