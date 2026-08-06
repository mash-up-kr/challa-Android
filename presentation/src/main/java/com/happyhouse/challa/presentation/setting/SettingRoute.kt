package com.happyhouse.challa.presentation.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarContent
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarHost
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarVisuals
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.setting.contract.SettingIntent
import com.happyhouse.challa.presentation.setting.contract.SettingSideEffect
import kotlinx.coroutines.launch

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
    val snackbarHostState = remember { SnackbarHostState() }
    val profileReadFailureMessage = stringResource(R.string.setting_profile_read_failure)
    val themeReadFailureMessage = stringResource(R.string.theme_read_failure)
    val retryLabel = stringResource(R.string.theme_retry)
    val destructiveIconTint = ChallaTheme.colors.statusDestructive

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            val (message, retryIntent) =
                when (effect) {
                    SettingSideEffect.ProfileReadFailed ->
                        profileReadFailureMessage to SettingIntent.ProfileReadRetry
                    SettingSideEffect.ThemeReadFailed ->
                        themeReadFailureMessage to SettingIntent.ThemeReadRetry
                }

            launch {
                val result =
                    snackbarHostState.showSnackbar(
                        ChallaSnackbarVisuals(
                            content =
                                ChallaSnackbarContent.HeadingOnly(
                                    heading = message,
                                ),
                            icon = ChallaIcons.Error,
                            iconTint = destructiveIconTint,
                            actionLabel = retryLabel,
                        ),
                    )

                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.onIntent(retryIntent)
                }
            }
        }
    }

    Box {
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

        ChallaSnackbarHost(hostState = snackbarHostState)
    }
}
