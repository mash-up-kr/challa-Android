package com.happyhouse.challa.presentation.designsystem.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

class ChallaPreviewWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        ChallaTheme {
            Box(
                modifier = Modifier
                    .background(ChallaTheme.colors.backgroundLevel4)
                    .padding(12.dp),
            ) {
                content()
            }
        }
    }
}
