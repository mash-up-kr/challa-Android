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
import com.happyhouse.challa.domain.repository.NotificationRepository
import com.happyhouse.challa.domain.result.ChallaResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject

/**
 * FCM registration token 갱신과 수신 메시지를 처리하는 Android 진입점입니다.
 *
 * 서버가 기존 registration token을 요구하므로 token이 교체될 때 [onNewToken]에서 서버 동기화를 시작합니다.
 * FCM이 자동으로 표시하지 않는 메시지는 콜백 반환 전에 서비스 알림 설정을 확인해 직접 알림을 생성하며,
 * 서비스 알림 설정이 꺼져 있거나 시스템 알림 권한이 없으면 표시하지 않습니다.
 */
@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var notificationTokenManager: NotificationTokenManager

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        notificationTokenManager.synchronize(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.d("FCM 메시지를 수신했습니다")

        // 콜백 반환 후 프로세스 종료로 알림 표시가 누락되지 않도록 설정 조회를 완료하되,
        // FCM의 짧은 처리 시간을 고려해 대기를 최대 5초로 제한한다.
        val notificationSetting =
            runBlocking {
                withTimeoutOrNull(NOTIFICATION_SETTING_TIMEOUT_MILLIS) {
                    notificationRepository.isEnabled.first()
                }
            }

        when (notificationSetting) {
            is ChallaResult.Success -> {
                if (notificationSetting.data) {
                    showNotification(message)
                } else {
                    Timber.d("서비스 알림이 비활성화되어 FCM 알림을 표시하지 않습니다")
                }
            }

            is ChallaResult.Failure -> Timber.w("서비스 알림 설정을 확인하지 못해 FCM 알림을 표시하지 않습니다")
            null ->
                Timber.w(
                    "서비스 알림 설정을 %dms 안에 확인하지 못해 FCM 알림을 표시하지 않습니다",
                    NOTIFICATION_SETTING_TIMEOUT_MILLIS,
                )
        }
    }

    private fun showNotification(message: RemoteMessage) {
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
        const val NOTIFICATION_SETTING_TIMEOUT_MILLIS = 5_000L
    }
}
