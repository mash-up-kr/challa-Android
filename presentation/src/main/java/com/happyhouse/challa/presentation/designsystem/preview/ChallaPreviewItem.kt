package com.happyhouse.challa.presentation.designsystem.preview

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

@Composable
fun ChallaPreviewItem(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Text(
        text = label,
        modifier = modifier,
        color = Color.White,
        style = ChallaTheme.typography.bodySmall,
    )
    Spacer(modifier = Modifier.height(12.dp))
    content()
}
