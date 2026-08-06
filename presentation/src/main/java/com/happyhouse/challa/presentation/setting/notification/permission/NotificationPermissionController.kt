package com.happyhouse.challa.presentation.setting.notification.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/**
 * 시스템 앱 알림 상태와 현재 상태에 맞는 권한 요청 동작을 제공합니다.
 *
 * Android 13 이상에서 권한을 직접 요청할 수 있으면 시스템 권한 다이얼로그를 표시하고,
 * 직접 요청할 수 없거나 Android 12 이하에서 앱 알림이 꺼져 있으면 앱 알림 설정을 엽니다.
 */
@Composable
fun rememberNotificationPermissionController(onRequestCompleted: (Boolean) -> Unit): NotificationPermissionController {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val currentOnRequestCompleted by rememberUpdatedState(onRequestCompleted)
    var isEnabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }

    fun refreshState(): Boolean =
        NotificationManagerCompat
            .from(context)
            .areNotificationsEnabled()
            .also { isEnabled = it }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) {
            currentOnRequestCompleted(refreshState())
        }
    val settingsLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) {
            currentOnRequestCompleted(refreshState())
        }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        refreshState()
    }

    return NotificationPermissionController(
        isEnabled = isEnabled,
        requestPermission = {
            val canRequestPermission =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !context.hasNotificationPermission() &&
                    (!hasRequestedPermission || activity.shouldShowNotificationPermissionRationale())

            if (canRequestPermission) {
                hasRequestedPermission = true
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                settingsLauncher.launch(context.notificationSettingsIntent())
            }
        },
    )
}

data class NotificationPermissionController(
    val isEnabled: Boolean,
    val requestPermission: () -> Unit,
)

private fun Context.hasNotificationPermission(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true

    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
}

private fun Activity?.shouldShowNotificationPermissionRationale(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || this == null) return false

    return ActivityCompat.shouldShowRequestPermissionRationale(
        this,
        Manifest.permission.POST_NOTIFICATIONS,
    )
}

private fun Context.notificationSettingsIntent(): Intent =
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
