package com.happyhouse.challa.presentation.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaListItem
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.layout.ChallaScaffold
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.setting.component.SettingProfile
import com.happyhouse.challa.presentation.setting.component.SettingSection
import com.happyhouse.challa.presentation.setting.component.SettingTopBar
import com.happyhouse.challa.presentation.setting.contract.SettingState
import com.happyhouse.challa.presentation.setting.theme.model.ThemeUiModel
import com.happyhouse.challa.presentation.setting.theme.titleRes

@Composable
fun SettingScreen(
    state: SettingState,
    onBackClick: () -> Unit,
    onProfileEditClick: () -> Unit,
    onThemeClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onAccountClick: () -> Unit,
    onSupportClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    // TODO: 배포를 위해 임시로 추가. 삭제 예정
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
) {
    val primaryThemeTitle = state.primaryTheme?.let { stringResource(it.titleRes) }

    ChallaScaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ChallaTheme.colors.backgroundSurface,
        snackbarHostState = snackbarHostState,
        topBar = {
            SettingTopBar(onBackClick = onBackClick)
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(ChallaTheme.colors.backgroundSurface)
                    .verticalScroll(rememberScrollState()),
        ) {
            SettingProfile(
                state = state,
                onEditClick = onProfileEditClick,
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SettingSection(
                    title = stringResource(R.string.setting_app_section),
                ) {
                    ChallaListItem(
                        text = stringResource(R.string.setting_theme),
                        leadingIcon = ChallaIcons.Palette,
                        trailingText = primaryThemeTitle,
                        onClick = onThemeClick,
                    )
                    ChallaListItem(
                        text = stringResource(R.string.setting_notification),
                        leadingIcon = ChallaIcons.Bell,
                        onClick = onNotificationClick,
                    )
                }

                SettingSection(
                    title = stringResource(R.string.setting_account_section),
                ) {
                    ChallaListItem(
                        text = stringResource(R.string.setting_account_management),
                        leadingIcon = ChallaIcons.Profile,
                        onClick = onAccountClick,
                    )
                }

                SettingSection(
                    title = stringResource(R.string.setting_feedback_section),
                ) {
                    ChallaListItem(
                        text = stringResource(R.string.setting_support),
                        leadingIcon = ChallaIcons.Carrot,
                        onClick = onSupportClick,
                    )
                    ChallaListItem(
                        text = stringResource(R.string.setting_send_feedback),
                        leadingIcon = ChallaIcons.Feedback,
                        onClick = onFeedbackClick,
                    )
                    // TODO: 배포를 위해 임시로 추가. 삭제 예정
                    ChallaListItem(
                        text = stringResource(R.string.setting_report),
                        leadingIcon = ChallaIcons.Error,
                        onClick = onReportClick,
                    )
                }
            }
        }
    }
}

@Preview
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun SettingScreenPreview() {
    SettingScreen(
        state =
            SettingState(
                nickname = "나는야멋쟁이토마토",
                profileImageUrl = "https://example.com/profile.jpg",
                isProfileLoaded = true,
                primaryTheme = ThemeUiModel.LEMONADE,
            ),
        onBackClick = {},
        onProfileEditClick = {},
        onThemeClick = {},
        onNotificationClick = {},
        onAccountClick = {},
        onSupportClick = {},
        onFeedbackClick = {},
        onReportClick = {},
    )
}
