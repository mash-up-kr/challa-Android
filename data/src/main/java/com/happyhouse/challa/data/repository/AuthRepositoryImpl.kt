package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.local.TokenDataStore
import com.happyhouse.challa.data.network.api.AuthApi
import com.happyhouse.challa.data.network.dto.request.LoginRequest
import com.happyhouse.challa.data.network.qualifier.RefreshClient
import com.happyhouse.challa.domain.model.AuthTokens
import com.happyhouse.challa.domain.repository.AuthRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import com.happyhouse.challa.domain.result.onSuccess
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl
@Inject
constructor(
    // 로그인은 인증이 불필요하므로, AuthInterceptor·TokenAuthenticator 가 없는 무인증 클라이언트를 쓴다.
    // (잘못된 idToken 에 서버가 401 을 주더라도 불필요한 refresh 시도·토큰 clear 로 이어지지 않게 한다.)
    @RefreshClient private val authApi: AuthApi,
    private val tokenDataStore: TokenDataStore,
) : AuthRepository {
    override suspend fun loginWithKakao(idToken: String): ChallaResult<AuthTokens> =
        authApi
            .login(
                LoginRequest(
                    provider = KAKAO_PROVIDER,
                    idToken = idToken,
                ),
            ).mapCatching { response ->
                check(response.success) { response.message }
                val data = requireNotNull(response.data) { "로그인 응답 데이터가 비어 있습니다." }
                AuthTokens(
                    accessToken = data.accessToken,
                    refreshToken = data.refreshToken,
                    isNewUser = data.isNew,
                )
            }.onSuccess { tokens ->
                tokenDataStore.saveTokens(tokens.accessToken, tokens.refreshToken)
            }

    override suspend fun isLoggedIn(): Boolean = tokenDataStore.accessToken.first() != null

    companion object {
        // 안드로이드 앱은 카카오 로그인만 지원한다. (서버 스펙상 APPLE 도 있으나 미구현)
        private const val KAKAO_PROVIDER = "KAKAO"
    }
}
