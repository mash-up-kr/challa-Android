package com.happyhouse.challa.presentation.setting.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.setting.theme.contract.ThemeIntent

@Composable
fun ThemeRoute(
    onBackClick: () -> Unit,
    viewModel: ThemeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ThemeScreen(
        selectedTheme = state.selectedTheme,
        onBackClick = onBackClick,
        onThemeClick = {
            viewModel.onIntent(ThemeIntent.ThemeSelect(it))
        },
    )
}
