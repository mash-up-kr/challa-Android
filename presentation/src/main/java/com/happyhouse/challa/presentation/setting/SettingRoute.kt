package com.happyhouse.challa.presentation.setting

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarContent
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarVisuals
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaToastVisuals
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.setting.contract.SettingIntent
import com.happyhouse.challa.presentation.setting.contract.SettingSideEffect
import com.happyhouse.challa.presentation.setting.contract.SettingState.ProfileState
import kotlinx.coroutines.launch

@Composable
fun SettingRoute(
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onProfileEditClick: (nickname: String, profileImageUrl: String?) -> Unit,
    onThemeClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onAccountClick: () -> Unit,
    onSupportClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onOpenSourceLicenseClick: () -> Unit,
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()
    // TODO: 인앱 신고·차단 기능 구현 전까지 쓰는 임시 값. 구현되면 아래 두 줄과 onReportAndBlockClick 삭제할 것.
    val reportAndBlockUrl = stringResource(R.string.setting_report_and_block_url)
    val reportAndBlockLinkOpenFailureMessage = stringResource(R.string.setting_report_and_block_link_open_failure)
    val profileReadFailureMessage = stringResource(R.string.setting_profile_read_failure)
    val themeReadFailureMessage = stringResource(R.string.theme_read_failure)
    val retryLabel = stringResource(R.string.theme_retry)
    val destructiveIconTint = ChallaTheme.colors.statusDestructive

    // 프로필 수정 후 설정 화면으로 돌아오면 최신 정보를 다시 조회한다.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onIntent(SettingIntent.FetchData)
    }

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

    SettingScreen(
        state = state,
        onBackClick = onBackClick,
        onProfileEditClick = {
            val profile = state.profile
            if (profile is ProfileState.Loaded) {
                onProfileEditClick(profile.nickname, profile.profileImageUrl)
            }
        },
        onThemeClick = onThemeClick,
        onNotificationClick = onNotificationClick,
        onAccountClick = onAccountClick,
        onReportAndBlockClick = {
            try {
                uriHandler.openUri(reportAndBlockUrl)
            } catch (_: IllegalArgumentException) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        ChallaToastVisuals(
                            message = reportAndBlockLinkOpenFailureMessage,
                            icon = ChallaIcons.Error,
                            iconTint = destructiveIconTint,
                        ),
                    )
                }
            }
        },
        onSupportClick = onSupportClick,
        onFeedbackClick = onFeedbackClick,
        onOpenSourceLicenseClick = onOpenSourceLicenseClick,
    )
}
