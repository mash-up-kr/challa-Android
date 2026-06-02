package com.happyhouse.challa

import androidx.compose.runtime.Composable
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.sample.SampleScreen

@Composable
fun ChallaApp() {
    ChallaTheme {
        SampleScreen()
    }
}
