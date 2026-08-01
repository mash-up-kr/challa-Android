package com.happyhouse.challa.presentation.setting.notification

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
fun NotificationRoute(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var systemNotificationsEnabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    var serviceNotificationsEnabled by rememberSaveable { mutableStateOf(true) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        systemNotificationsEnabled =
            NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    NotificationScreen(
        systemNotificationsEnabled = systemNotificationsEnabled,
        serviceNotificationsEnabled = serviceNotificationsEnabled,
        onBackClick = onBackClick,
        onSystemNotificationSettingClick = {
            context.startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
            )
        },
        onServiceNotificationEnabledChange = { enabled ->
            serviceNotificationsEnabled = enabled
        },
    )
}
