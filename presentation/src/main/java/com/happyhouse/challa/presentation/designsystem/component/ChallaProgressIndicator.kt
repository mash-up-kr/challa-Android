package com.happyhouse.challa.presentation.designsystem.component

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

@Composable
fun ChallaProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = ChallaTheme.colors.primary,
) {
    CircularProgressIndicator(
        modifier = modifier,
        color = color,
    )
}

@Preview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaProgressIndicatorPreview() {
    ChallaProgressIndicator()
}
