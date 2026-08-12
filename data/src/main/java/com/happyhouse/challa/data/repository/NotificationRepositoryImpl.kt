package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.local.NotificationSettingsDataStore
import com.happyhouse.challa.data.local.TokenDataStore
import com.happyhouse.challa.data.network.api.NotificationApi
import com.happyhouse.challa.data.network.dto.request.NotificationTokenRequest
import com.happyhouse.challa.data.network.dto.request.TestNotificationRequest
import com.happyhouse.challa.domain.repository.NotificationRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
    override val isEnabled: Flow<ChallaResult<Boolean>> = notificationSettingsDataStore.isEnabled

    override suspend fun setEnabled(enabled: Boolean): ChallaResult<Unit> =
        try {
            notificationSettingsDataStore.setEnabled(enabled)
            ChallaResult.Success(Unit)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            ChallaResult.Failure.Unknown(throwable)
        }

    override suspend fun updatePushToken(token: String): ChallaResult<Unit> =
        try {
            val savedToken = notificationSettingsDataStore.pushToken.first()
            val isLoggedIn = !tokenDataStore.accessToken.first().isNullOrBlank()

            if (isLoggedIn && savedToken != null && savedToken != token) {
                when (val result = deleteToken(savedToken)) {
                    is ChallaResult.Success -> Unit
                    is ChallaResult.Failure -> return result
                }
            }

            notificationSettingsDataStore.savePushToken(token)
            registerSavedPushToken()
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            ChallaResult.Failure.Unknown(throwable)
        }

    override suspend fun registerSavedPushToken(): ChallaResult<Unit> =
        try {
            if (tokenDataStore.accessToken.first().isNullOrBlank()) return ChallaResult.Success(Unit)
            val token = notificationSettingsDataStore.pushToken.first() ?: return ChallaResult.Success(Unit)

            registerToken(token)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            ChallaResult.Failure.Unknown(throwable)
        }

    override suspend fun deleteSavedPushToken(): ChallaResult<Unit> =
        try {
            val token = notificationSettingsDataStore.pushToken.first() ?: return ChallaResult.Success(Unit)

            deleteToken(token)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            ChallaResult.Failure.Unknown(throwable)
        }

    override suspend fun sendTestPush(
        title: String?,
        body: String?,
    ): ChallaResult<Int> =
        notificationApi
            .sendTestNotification(
                TestNotificationRequest(
                    notification =
                        TestNotificationRequest.Notification(
                            title = title,
                            body = body,
                        ),
                ),
            ).mapCatching { response ->
                check(response.success) { response.message }
                requireNotNull(response.data) { "테스트 푸시 응답 데이터가 비어 있습니다." }
                    .notification
                    .sentCount
            }

    private fun String.toRequest(): NotificationTokenRequest =
        NotificationTokenRequest(
            notification = NotificationTokenRequest.Notification(token = this),
        )

    private suspend fun registerToken(token: String): ChallaResult<Unit> =
        notificationApi
            .registerToken(token.toRequest())
            .mapCatching { response -> check(response.success) { response.message } }

    private suspend fun deleteToken(token: String): ChallaResult<Unit> =
        notificationApi
            .deleteToken(token.toRequest())
            .mapCatching { response -> check(response.success) { response.message } }
}
