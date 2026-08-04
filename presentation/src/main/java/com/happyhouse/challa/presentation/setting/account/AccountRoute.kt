package com.happyhouse.challa.presentation.setting.account

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarContent
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarVisuals
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.setting.SettingViewModel
import com.happyhouse.challa.presentation.setting.account.contract.AccountIntent
import com.happyhouse.challa.presentation.setting.account.contract.AccountSideEffect
import kotlinx.coroutines.launch

@Composable
fun AccountRoute(
    onBackClick: () -> Unit,
    onLogoutSuccess: () -> Unit,
    onWithdrawClick: () -> Unit,
    settingViewModel: SettingViewModel = hiltViewModel(),
    accountViewModel: AccountViewModel = hiltViewModel(),
) {
    val settingState by settingViewModel.uiState.collectAsStateWithLifecycle()
    val accountState by accountViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val logoutFailureMessage = stringResource(R.string.account_logout_failure)
    val destructiveIconTint = ChallaTheme.colors.statusDestructive

    LaunchedEffect(accountViewModel) {
        accountViewModel.uiEffect.collect { effect ->
            when (effect) {
                AccountSideEffect.LogoutSuccess -> onLogoutSuccess()
                AccountSideEffect.LogoutFailed ->
                    launch {
                        snackbarHostState.showSnackbar(
                            ChallaSnackbarVisuals(
                                content =
                                    ChallaSnackbarContent.HeadingOnly(
                                        heading = logoutFailureMessage,
                                    ),
                                icon = ChallaIcons.Error,
                                iconTint = destructiveIconTint,
                            ),
                        )
                    }
            }
        }
    }

    AccountScreen(
        nickname = settingState.nickname,
        maskedEmail = settingState.maskedEmail,
        isLoggingOut = accountState.isLoggingOut,
        onBackClick = onBackClick,
        onLogoutClick = { accountViewModel.onIntent(AccountIntent.LogoutClick) },
        onWithdrawClick = onWithdrawClick,
        snackbarHostState = snackbarHostState,
    )
}
