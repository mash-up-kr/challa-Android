package com.happyhouse.challa.presentation.setting.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.setting.notification.permission.rememberNotificationPermissionController

@Composable
fun NotificationRoute(
    onBackClick: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel(),
) {
    val isEnabled by viewModel.isEnabled.collectAsStateWithLifecycle()
    var pendingServiceNotificationEnable by rememberSaveable { mutableStateOf(false) }
    val permissionController =
        rememberNotificationPermissionController { isSystemNotificationEnabled ->
            if (pendingServiceNotificationEnable && isSystemNotificationEnabled) {
                viewModel.setEnabled(true)
            }
            pendingServiceNotificationEnable = false
        }

    LaunchedEffect(permissionController.isEnabled, isEnabled) {
        if (!permissionController.isEnabled && isEnabled) {
            viewModel.setEnabled(false)
        }
    }

    NotificationScreen(
        systemNotificationsEnabled = permissionController.isEnabled,
        serviceNotificationsEnabled = isEnabled && permissionController.isEnabled,
        onBackClick = onBackClick,
        onSystemNotificationSettingClick = permissionController.requestPermission,
        onServiceNotificationEnabledChange = { enabled ->
            when {
                !enabled -> viewModel.setEnabled(false)
                permissionController.isEnabled -> viewModel.setEnabled(true)
                else -> {
                    pendingServiceNotificationEnable = true
                    permissionController.requestPermission()
                }
            }
        },
    )
}
