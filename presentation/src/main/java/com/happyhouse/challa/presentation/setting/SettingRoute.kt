package com.happyhouse.challa.presentation.setting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingRoute(
    onBackClick: () -> Unit,
    onProfileEditClick: () -> Unit,
    onThemeClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onAccountClick: () -> Unit,
    onSupportClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SettingScreen(
        state = state,
        onBackClick = onBackClick,
        onProfileEditClick = onProfileEditClick,
        onThemeClick = onThemeClick,
        onNotificationClick = onNotificationClick,
        onAccountClick = onAccountClick,
        onSupportClick = onSupportClick,
        onFeedbackClick = onFeedbackClick,
    )
}
