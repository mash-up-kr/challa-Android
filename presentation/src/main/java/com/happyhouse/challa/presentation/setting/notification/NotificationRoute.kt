package com.happyhouse.challa.presentation.setting.notification

import androidx.compose.material3.SnackbarHostState
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
import com.happyhouse.challa.presentation.setting.notification.permission.rememberNotificationPermissionController
import kotlinx.coroutines.launch

@Composable
fun NotificationRoute(
    onBackClick: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel(),
) {
    val isEnabled by viewModel.isEnabled.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val saveFailureMessage = stringResource(R.string.notification_save_failure)
    val destructiveIconTint = ChallaTheme.colors.statusDestructive
    var pendingServiceNotificationEnable by rememberSaveable { mutableStateOf(false) }
    val permissionController =
        rememberNotificationPermissionController { isSystemNotificationEnabled ->
            if (pendingServiceNotificationEnable && isSystemNotificationEnabled) {
                viewModel.onEnabledChange(true)
            }
            pendingServiceNotificationEnable = false
        }

    LaunchedEffect(viewModel) {
        viewModel.saveFailure.collect {
            launch {
                snackbarHostState.showSnackbar(
                    ChallaSnackbarVisuals(
                        content =
                            ChallaSnackbarContent.HeadingOnly(
                                heading = saveFailureMessage,
                            ),
                        icon = ChallaIcons.Error,
                        iconTint = destructiveIconTint,
                    ),
                )
            }
        }
    }

    LaunchedEffect(permissionController.isEnabled, isEnabled) {
        if (!permissionController.isEnabled && isEnabled) {
            viewModel.onEnabledChange(false)
        }
    }

    NotificationScreen(
        systemNotificationsEnabled = permissionController.isEnabled,
        serviceNotificationsEnabled = isEnabled && permissionController.isEnabled,
        onBackClick = onBackClick,
        onSystemNotificationSettingClick = permissionController.requestPermission,
        onServiceNotificationEnabledChange = { enabled ->
            when {
                !enabled -> viewModel.onEnabledChange(false)
                permissionController.isEnabled -> viewModel.onEnabledChange(true)

                else -> {
                    pendingServiceNotificationEnable = true
                    permissionController.requestPermission()
                }
            }
        },
        snackbarHostState = snackbarHostState,
    )
}
