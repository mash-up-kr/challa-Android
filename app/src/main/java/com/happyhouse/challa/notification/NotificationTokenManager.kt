package com.happyhouse.challa.notification

import com.google.firebase.messaging.FirebaseMessaging
import com.happyhouse.challa.di.qualifier.ApplicationScope
import com.happyhouse.challa.domain.repository.NotificationRepository
import com.happyhouse.challa.domain.result.ChallaResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FCM registration token을 조회하고 앱 서버의 현재 사용자에게 동기화합니다.
 *
 * 서버가 아직 FID가 아닌 기존 registration token을 요구하므로 deprecated FCM token API를 사용합니다.
 * 앱 수명의 scope를 사용해 서비스가 종료돼도 진행 중인 동기화를 보존합니다.
 * 최초 token 생성 시 앱 시작 조회와 [ChallaFirebaseMessagingService.onNewToken]이 같은 token을 동시에 전달할 수 있어
 * 동기화를 직렬화하고, 같은 프로세스에서 이미 성공한 token의 중복 동기화를 생략합니다.
 * 실패한 token은 성공한 것으로 기록하지 않아 다음 호출에서 다시 시도합니다.
 */
@Singleton
class NotificationTokenManager @Inject constructor(
    private val notificationRepository: NotificationRepository,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
) {
    private val synchronizationMutex = Mutex()
    private var syncedToken: String? = null

    /**
     * 앱 시작 시 현재 registration token을 조회해 로컬 저장소와 앱 서버에 동기화합니다.
     *
     * 현재 서버가 기존 registration token을 받는 계약이어서 deprecated API를 사용합니다.
     * 추후 서버와 논의해 Firebase 권장 방식인 FID 기반 등록으로 전환할 수 있습니다.
     */
    @Suppress("DEPRECATION")
    fun synchronizeCurrentToken() {
        FirebaseMessaging
            .getInstance()
            .token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    synchronize(task.result)
                } else {
                    Timber.w(task.exception, "FCM 등록 토큰을 가져오지 못했습니다")
                }
            }
    }

    /** 새 token을 로컬에 저장하고, 로그인 상태이면 앱 서버에도 반영합니다. */
    fun synchronize(token: String) {
        applicationScope.launch {
            synchronizationMutex.withLock {
                if (syncedToken == token) return@withLock

                when (notificationRepository.updatePushToken(token)) {
                    is ChallaResult.Success -> {
                        syncedToken = token
                        Timber.d("FCM 등록 토큰을 동기화했습니다")
                    }

                    is ChallaResult.Failure -> Timber.w("FCM 등록 토큰을 동기화하지 못했습니다")
                }
            }
        }
    }
}
