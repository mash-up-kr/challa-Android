package com.happyhouse.challa.presentation.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonSize
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonVariant
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaIconButton
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaTextButton
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
    content: @Composable () -> Unit,
) {
    ChallaDrawerContainer(
        onDismissRequest = onDismissRequest,
    ) {
        ChallaDrawerSurface(
            variant = variant,
            title = title,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            content = content,
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
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                .clip(shape = RoundedCornerShape(32.dp))
                .background(ChallaTheme.colors.backgroundLevel1)
                .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (variant) {
            ChallaDrawerVariant.COMPACT ->
                ChallaCompactDrawerContent(content = content)

            ChallaDrawerVariant.RICH ->
                ChallaRichDrawerContent(
                    title = title,
                    onDismissRequest = onDismissRequest,
                    content = content,
                )
        }
    }
}

@Composable
private fun ChallaCompactDrawerContent(content: @Composable () -> Unit) {
    Spacer(modifier = Modifier.height(12.dp))
    ChallaDrawerHandle()
    Spacer(modifier = Modifier.height(24.dp))
    content()
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun ChallaRichDrawerContent(
    title: String,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier =
                Modifier
                    .padding(vertical = 24.dp)
                    .weight(1f),
            color = ChallaTheme.colors.labelNormal,
            style = ChallaTheme.typography.bodyLarge.bold,
        )
        ChallaIconButton(
            icon = ChallaIcons.Close,
            onClick = onDismissRequest,
            contentDescription = "닫기",
            variant = ChallaButtonVariant.TRANSPARENT,
            size = ChallaButtonSize.LARGE,
        )
    }
    HorizontalDivider(color = ChallaTheme.colors.lineNeutral)
    content()
    Spacer(modifier = Modifier.height(24.dp))
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
    ) {
        ChallaDrawerPreviewActions()
    }
}

@Preview(widthDp = 390, heightDp = 720)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaRichDrawerPreview() {
    ChallaDrawerSurface(
        variant = ChallaDrawerVariant.RICH,
        title = "타이틀",
        onDismissRequest = {},
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Instance Slot",
                color = ChallaTheme.colors.labelNormal,
                style = ChallaTheme.typography.bodySmall.bold,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        ChallaDrawerPreviewActions()
    }
}

@Composable
private fun ChallaDrawerPreviewActions() {
    ChallaDrawerPreviewPrimaryButton()
    Spacer(modifier = Modifier.height(8.dp))
    ChallaDrawerPreviewNeutralButton()
    Spacer(modifier = Modifier.height(8.dp))
    ChallaDrawerPreviewTransparentButton()
}

@Composable
private fun ChallaDrawerPreviewPrimaryButton() {
    ChallaTextButton(
        text = "버튼명",
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ChallaDrawerPreviewNeutralButton() {
    ChallaTextButton(
        text = "버튼명",
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
        variant = ChallaButtonVariant.NEUTRAL,
    )
}

@Composable
private fun ChallaDrawerPreviewTransparentButton() {
    ChallaTextButton(
        text = "보조 액션",
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
        variant = ChallaButtonVariant.TRANSPARENT,
        size = ChallaButtonSize.MEDIUM,
    )
}
