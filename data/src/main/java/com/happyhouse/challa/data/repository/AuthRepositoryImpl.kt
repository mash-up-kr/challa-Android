package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.local.ThemeDataStore
import com.happyhouse.challa.data.local.TokenDataStore
import com.happyhouse.challa.data.local.UserProfileCache
import com.happyhouse.challa.data.network.api.AuthApi
import com.happyhouse.challa.data.network.dto.LogoutRequest
import com.happyhouse.challa.data.network.dto.request.LoginRequest
import com.happyhouse.challa.data.network.qualifier.RefreshClient
import com.happyhouse.challa.domain.model.AuthTokens
import com.happyhouse.challa.domain.repository.AuthRepository
import com.happyhouse.challa.domain.repository.NotificationRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import com.happyhouse.challa.domain.result.onSuccess
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    // 로그인은 인증이 불필요하므로, AuthInterceptor·TokenAuthenticator 가 없는 무인증 클라이언트를 쓴다.
    // (잘못된 idToken 에 서버가 401 을 주더라도 불필요한 refresh 시도·토큰 clear 로 이어지지 않게 한다.)
    @param:RefreshClient private val unauthenticatedAuthApi: AuthApi,
    private val authApi: AuthApi,
    private val tokenDataStore: TokenDataStore,
    private val themeDataStore: ThemeDataStore,
    private val userProfileCache: UserProfileCache,
    private val notificationRepository: NotificationRepository,
) : AuthRepository {
    override suspend fun loginWithKakao(idToken: String): ChallaResult<AuthTokens> =
        unauthenticatedAuthApi
            .login(
                LoginRequest(
                    auth =
                        LoginRequest.Auth(
                            provider = KAKAO_PROVIDER,
                            idToken = idToken,
                        ),
                ),
            ).mapCatching { response ->
                check(response.success) { response.message }
                val auth = requireNotNull(response.data) { "로그인 응답 데이터가 비어 있습니다." }.auth
                AuthTokens(
                    accessToken = auth.accessToken,
                    refreshToken = auth.refreshToken,
                    isNewUser = auth.isNew,
                )
            }.onSuccess { tokens ->
                userProfileCache.clear()
                tokenDataStore.saveTokens(tokens.accessToken, tokens.refreshToken)
                when (notificationRepository.registerSavedPushToken()) {
                    is ChallaResult.Success -> Unit
                    is ChallaResult.Failure -> Logger.w("로그인 후 FCM 등록 토큰을 서버에 등록하지 못했습니다")
                }
            }

    override suspend fun logout(): ChallaResult<Unit> =
        try {
            // FCM 토큰 정리 실패가 사용자의 로그아웃을 막지 않도록 best-effort로 처리한다.
            when (notificationRepository.deleteSavedPushToken()) {
                is ChallaResult.Success -> Unit
                is ChallaResult.Failure -> Logger.w("FCM 등록 토큰을 삭제하지 못했지만 로그아웃을 계속합니다")
            }

            val refreshToken =
                requireNotNull(tokenDataStore.refreshToken.first()) {
                    "저장된 리프레시 토큰이 없습니다."
                }

            authApi
                .logout(
                    LogoutRequest(
                        auth = LogoutRequest.Auth(refreshToken = refreshToken),
                    ),
                ).mapCatching { response ->
                    check(response.success) { response.message }
                }.onSuccess {
                    themeDataStore.clearPrimaryTheme()
                    tokenDataStore.clear()
                    userProfileCache.clear()
                }
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            ChallaResult.Failure.Unknown(throwable)
        }

    override suspend fun isLoggedIn(): Boolean = tokenDataStore.accessToken.first() != null

    companion object {
        // 안드로이드 앱은 카카오 로그인만 지원한다. (서버 스펙상 APPLE 도 있으나 미구현)
        private const val KAKAO_PROVIDER = "KAKAO"
    }
}
