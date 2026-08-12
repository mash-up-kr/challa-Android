package com.happyhouse.challa.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
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
    }
}
