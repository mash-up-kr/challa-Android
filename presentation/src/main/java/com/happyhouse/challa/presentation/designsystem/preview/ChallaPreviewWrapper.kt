package com.happyhouse.challa.presentation.designsystem.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

class ChallaPreviewWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        ChallaTheme {
            content()
        }
    }
}
