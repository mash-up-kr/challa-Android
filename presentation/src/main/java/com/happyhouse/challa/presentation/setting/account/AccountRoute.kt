package com.happyhouse.challa.presentation.setting.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.setting.SettingViewModel

@Composable
fun AccountRoute(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AccountScreen(
        nickname = state.nickname,
        maskedEmail = state.maskedEmail,
        onBackClick = onBackClick,
        onLogoutClick = onLogoutClick,
        onWithdrawClick = onWithdrawClick,
    )
}
