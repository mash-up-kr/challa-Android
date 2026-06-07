package com.happyhouse.challa

import androidx.compose.runtime.Composable
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.navigation.ChallaNavHost

@Composable
fun ChallaApp() {
    ChallaTheme {
        ChallaNavHost()
    }
}
