package com.happyhouse.challa.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.happyhouse.challa.R

/** FCM 자동 알림과 앱이 직접 게시하는 알림에 공통으로 사용할 기본 채널입니다. */
object ChallaNotificationChannel {
    /** Android 8.0 이상에서 알림을 게시할 수 있도록 앱 시작 시 기본 채널을 등록합니다. */
    fun create(context: Context) {
        val channel =
            NotificationChannel(
                context.getString(R.string.default_notification_channel_id),
                context.getString(R.string.general_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.general_notification_channel_description)
            }

        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
