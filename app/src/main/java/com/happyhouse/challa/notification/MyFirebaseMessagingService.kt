package com.happyhouse.challa.notification

import android.Manifest
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.happyhouse.challa.R
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * FCM registration token 갱신과 수신 메시지를 처리하는 Android 진입점입니다.
 *
 * 서버가 기존 registration token을 요구하므로 token이 교체될 때 [onNewToken]에서 서버 동기화를 시작합니다.
 */
@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var notificationTokenManager: NotificationTokenManager

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        notificationTokenManager.synchronize(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.d(
            // TODO BJ: 개발 후에 메시지, 발신자, 키 제거
            "FCM 메시지를 수신했습니다: 메시지 ID=%s, 발신자=%s, 데이터 키=%s",
            message.messageId,
            message.from,
            message.data.keys,
        )

        if (!canPostNotifications()) {
            Timber.d("알림 권한이 없거나 시스템 알림이 비활성화되어 FCM 알림을 표시하지 않습니다")
            return
        }

        val title =
            message.notification?.title ?: message.data[TITLE_KEY] ?: getString(R.string.app_name)
        val body = message.notification?.body ?: message.data[BODY_KEY]
        if (body.isNullOrBlank()) {
            Timber.w("FCM 메시지에 알림 본문이 없어 알림을 표시하지 않습니다")
            return
        }

        val notificationId = message.messageId?.hashCode() ?: message.sentTime.hashCode()
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent == null) {
            Timber.w("앱 실행 Intent를 만들지 못해 FCM 알림을 표시하지 않습니다")
            return
        }
        val contentIntent =
            PendingIntent.getActivity(
                this,
                notificationId,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            NotificationCompat
                .Builder(this, getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build()

        try {
            NotificationManagerCompat.from(this).notify(notificationId, notification)
        } catch (exception: SecurityException) {
            Timber.w(exception, "알림 권한이 없어 FCM 알림을 표시하지 못했습니다")
        }
    }

    private fun canPostNotifications(): Boolean {
        val hasPermission =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED

        return hasPermission && NotificationManagerCompat.from(this).areNotificationsEnabled()
    }

    private companion object {
        const val TITLE_KEY = "title"
        const val BODY_KEY = "body"
    }
}
