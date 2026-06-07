package com.happyhouse.challa.ui

import androidx.compose.runtime.Composable
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.navigation.ChallaNavHost

@Composable
fun ChallaApp() {
    val appState = rememberChallaAppState()

    ChallaTheme {
        ChallaNavHost(
            navigator = appState.navigator,
        )
    }
}
