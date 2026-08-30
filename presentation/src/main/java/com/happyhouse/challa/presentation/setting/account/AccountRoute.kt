package com.happyhouse.challa.presentation.setting.account

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarContent
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarVisuals
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.setting.account.contract.AccountIntent
import com.happyhouse.challa.presentation.setting.account.contract.AccountSideEffect
import kotlinx.coroutines.launch

@Composable
fun AccountRoute(
    onBackClick: () -> Unit,
    onLogoutSuccess: () -> Unit,
    onWithdrawSuccess: () -> Unit,
    accountViewModel: AccountViewModel = hiltViewModel(),
) {
    val accountState by accountViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val profileReadFailureMessage = stringResource(R.string.setting_profile_read_failure)
    val logoutFailureMessage = stringResource(R.string.account_logout_failure)
    val withdrawalFailureMessage = stringResource(R.string.account_withdraw_failure)
    val retryLabel = stringResource(R.string.theme_retry)
    val destructiveIconTint = ChallaTheme.colors.statusDestructive
    var withdrawalDrawerState by rememberSaveable {
        mutableStateOf<WithdrawalDrawerState?>(null)
    }

    LaunchedEffect(accountViewModel) {
        accountViewModel.uiEffect.collect { effect ->
            when (effect) {
                AccountSideEffect.ProfileReadFailed ->
                    launch {
                        val result =
                            snackbarHostState.showSnackbar(
                                ChallaSnackbarVisuals(
                                    content =
                                        ChallaSnackbarContent.HeadingOnly(
                                            heading = profileReadFailureMessage,
                                        ),
                                    icon = ChallaIcons.Error,
                                    iconTint = destructiveIconTint,
                                    actionLabel = retryLabel,
                                ),
                            )

                        if (result == SnackbarResult.ActionPerformed) {
                            accountViewModel.onIntent(AccountIntent.ProfileReadRetry)
                        }
                    }

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

                AccountSideEffect.WithdrawalSuccess -> {
                    withdrawalDrawerState = WithdrawalDrawerState.COMPLETED
                }

                AccountSideEffect.WithdrawalFailed -> {
                    withdrawalDrawerState = null
                    launch {
                        snackbarHostState.showSnackbar(
                            ChallaSnackbarVisuals(
                                content =
                                    ChallaSnackbarContent.HeadingOnly(
                                        heading = withdrawalFailureMessage,
                                    ),
                                icon = ChallaIcons.Error,
                                iconTint = destructiveIconTint,
                            ),
                        )
                    }
                }
            }
        }
    }

    AccountScreen(
        nickname = accountState.nickname,
        profileImageUrl = accountState.profileImageUrl,
        isProcessing = accountState.isProcessing,
        onBackClick = onBackClick,
        onLogoutClick = { accountViewModel.onIntent(AccountIntent.LogoutClick) },
        onWithdrawClick = {
            withdrawalDrawerState = WithdrawalDrawerState.CONFIRMATION
        },
        snackbarHostState = snackbarHostState,
    )

    when (withdrawalDrawerState) {
        WithdrawalDrawerState.CONFIRMATION ->
            AccountWithdrawalConfirmationDrawer(
                isWithdrawing = accountState.isWithdrawing,
                onConfirmClick = {
                    accountViewModel.onIntent(AccountIntent.WithdrawalConfirmClick)
                },
                onDismissRequest = { withdrawalDrawerState = null },
            )

        WithdrawalDrawerState.COMPLETED ->
            AccountWithdrawalCompletedDrawer(
                onConfirmClick = onWithdrawSuccess,
            )

        null -> Unit
    }
}

private enum class WithdrawalDrawerState {
    CONFIRMATION,
    COMPLETED,
}
