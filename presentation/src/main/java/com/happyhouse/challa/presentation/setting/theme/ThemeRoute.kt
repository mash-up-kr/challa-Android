package com.happyhouse.challa.presentation.setting.theme

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
import com.happyhouse.challa.presentation.setting.theme.contract.ThemeIntent
import com.happyhouse.challa.presentation.setting.theme.contract.ThemeSideEffect
import kotlinx.coroutines.launch

@Composable
fun ThemeRoute(
    onBackClick: () -> Unit,
    viewModel: ThemeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val saveFailureMessage = stringResource(R.string.theme_save_failure)
    val retryLabel = stringResource(R.string.theme_retry)
    val destructiveIconTint = ChallaTheme.colors.statusDestructive

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ThemeSideEffect.SaveFailed ->
                    launch {
                        val result =
                            snackbarHostState.showSnackbar(
                                ChallaSnackbarVisuals(
                                    content =
                                        ChallaSnackbarContent.HeadingOnly(
                                            heading = saveFailureMessage,
                                        ),
                                    icon = ChallaIcons.Error,
                                    iconTint = destructiveIconTint,
                                    actionLabel = retryLabel,
                                ),
                            )

                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.onIntent(ThemeIntent.ThemeSelect(effect.theme))
                        }
                    }
            }
        }
    }

    Box {
        ThemeScreen(
            selectedTheme = state.selectedTheme,
            onBackClick = onBackClick,
            onThemeClick = {
                viewModel.onIntent(ThemeIntent.ThemeSelect(it))
            },
        )

        ChallaSnackbarHost(hostState = snackbarHostState)
    }
}
