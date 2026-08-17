package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.local.NotificationSettingsDataStore
import com.happyhouse.challa.data.local.TokenDataStore
import com.happyhouse.challa.data.network.api.NotificationApi
import com.happyhouse.challa.data.network.dto.request.NotificationTokenRequest
import com.happyhouse.challa.domain.repository.NotificationRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 알림 사용 설정은 DataStore에 보관하고 FCM registration token은 인증된 알림 API와 동기화합니다.
 * 로그인 전 발급된 token도 로컬에 보관했다가 로그인 성공 후 서버에 등록합니다.
 */
@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationSettingsDataStore: NotificationSettingsDataStore,
    private val tokenDataStore: TokenDataStore,
    private val notificationApi: NotificationApi,
) : NotificationRepository {
    /** 토글 변경과 token 갱신·로그인·로그아웃이 겹쳐 서버 등록 상태가 역전되는 것을 방지합니다. */
    private val tokenSynchronizationMutex = Mutex()

    override val isEnabled: Flow<ChallaResult<Boolean>> = notificationSettingsDataStore.isEnabled

    override suspend fun setEnabled(enabled: Boolean): ChallaResult<Unit> =
        tokenSynchronizationMutex.withLock {
            try {
                val synchronizationResult =
                    if (enabled) {
                        registerSavedPushTokenInternal()
                    } else {
                        deleteSavedPushTokenInternal()
                    }
                if (synchronizationResult is ChallaResult.Failure) {
                    return@withLock synchronizationResult
                }

                notificationSettingsDataStore.setEnabled(enabled)
                ChallaResult.Success(Unit)
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                ChallaResult.Failure.Unknown(throwable)
            }
        }

    override suspend fun updatePushToken(token: String): ChallaResult<Unit> =
        tokenSynchronizationMutex.withLock {
            try {
                val notificationsEnabled =
                    when (val result = notificationSettingsDataStore.isEnabled.first()) {
                        is ChallaResult.Success -> result.data
                        is ChallaResult.Failure -> return@withLock result
                    }
                val savedToken = notificationSettingsDataStore.pushToken.first()
                val isLoggedIn = !tokenDataStore.accessToken.first().isNullOrBlank()

                if (!notificationsEnabled || !isLoggedIn) {
                    notificationSettingsDataStore.savePushToken(token)
                    return@withLock ChallaResult.Success(Unit)
                }

                when (val result = registerToken(token)) {
                    is ChallaResult.Success -> Unit
                    is ChallaResult.Failure -> return@withLock result
                }
                notificationSettingsDataStore.savePushToken(token)

                // 이전 토큰 정리 실패가 새 토큰 동기화를 막지 않도록 best-effort로 처리한다.
                if (savedToken != null && savedToken != token) {
                    when (deleteToken(savedToken)) {
                        is ChallaResult.Success -> Unit
                        is ChallaResult.Failure -> Logger.w("이전 FCM 등록 토큰을 삭제하지 못했습니다")
                    }
                }

                ChallaResult.Success(Unit)
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                ChallaResult.Failure.Unknown(throwable)
            }
        }

    override suspend fun registerSavedPushToken(): ChallaResult<Unit> =
        tokenSynchronizationMutex.withLock {
            try {
                when (val result = notificationSettingsDataStore.isEnabled.first()) {
                    is ChallaResult.Success -> {
                        if (result.data) registerSavedPushTokenInternal() else ChallaResult.Success(Unit)
                    }

                    is ChallaResult.Failure -> result
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                ChallaResult.Failure.Unknown(throwable)
            }
        }

    override suspend fun deleteSavedPushToken(): ChallaResult<Unit> =
        tokenSynchronizationMutex.withLock {
            try {
                deleteSavedPushTokenInternal()
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                ChallaResult.Failure.Unknown(throwable)
            }
        }

    private fun String.toRequest(): NotificationTokenRequest =
        NotificationTokenRequest(
            notification = NotificationTokenRequest.Notification(token = this),
        )

    private suspend fun registerToken(token: String): ChallaResult<Unit> =
        notificationApi
            .registerToken(token.toRequest())
            .mapCatching { response -> check(response.success) { response.message } }

    private suspend fun registerSavedPushTokenInternal(): ChallaResult<Unit> {
        if (tokenDataStore.accessToken.first().isNullOrBlank()) return ChallaResult.Success(Unit)
        val token = notificationSettingsDataStore.pushToken.first() ?: return ChallaResult.Success(Unit)

        return registerToken(token)
    }

    private suspend fun deleteSavedPushTokenInternal(): ChallaResult<Unit> {
        if (tokenDataStore.accessToken.first().isNullOrBlank()) return ChallaResult.Success(Unit)
        val token = notificationSettingsDataStore.pushToken.first() ?: return ChallaResult.Success(Unit)

        return deleteToken(token)
    }

    private suspend fun deleteToken(token: String): ChallaResult<Unit> =
        notificationApi
            .deleteToken(token.toRequest())
            .mapCatching { response -> check(response.success) { response.message } }
}
