package com.happyhouse.challa.presentation.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

@Composable
fun SampleScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Challa Sample")
    }
}

@Preview(showBackground = true)
@Composable
private fun SampleScreenPreview() {
    ChallaTheme {
        SampleScreen()
    }
}
