package com.happyhouse.challa.data.network.interceptor

import com.happyhouse.challa.data.local.TokenDataStore
import com.happyhouse.challa.data.local.UserProfileCache
import com.happyhouse.challa.data.network.api.AuthApi
import com.happyhouse.challa.data.network.dto.request.RefreshRequest
import com.happyhouse.challa.data.network.qualifier.RefreshClient
import com.happyhouse.challa.domain.result.ChallaResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

/**
 * 401(Unauthorized) 응답을 받으면 저장된 refresh token으로 토큰을 재발급하고 원래 요청을 재시도한다.
 *
 * 재발급 요청은 인증 인터셉터와 본 Authenticator를 거치지 않는 [RefreshClient] 전용 [AuthApi]로 전송해
 * 무한 루프와 데드락을 방지한다. 토큰을 갱신할 수 없거나 갱신된 토큰도 거부되면 인증 정보와
 * 사용자 프로필 캐시를 초기화하고 재시도를 중단한다.
 */
class TokenAuthenticator
    @Inject
    constructor(
        private val tokenDataStore: TokenDataStore,
        private val userProfileCache: UserProfileCache,
        @param:RefreshClient private val refreshApi: AuthApi,
    ) : Authenticator {
        @Synchronized
        override fun authenticate(
            route: Route?,
            response: Response,
        ): Request? {
            // 새 토큰으로도 계속 401 이면(서버가 재발급 토큰을 거부) 무한 재시도를 막는다.
            if (responseCount(response) >= MAX_ATTEMPTS) {
                clearSession()
                return null
            }

            val failedToken = response.request.header(AUTHORIZATION)?.removePrefix(BEARER_PREFIX)

            // 다른 요청이 이미 토큰을 갱신해 두었다면, 재발급 없이 갱신된 토큰으로 바로 재시도한다.
            val storedAccess = runBlocking { tokenDataStore.accessToken.first() }
            if (!storedAccess.isNullOrBlank() && storedAccess != failedToken) {
                return response.request.withBearer(storedAccess)
            }

            val refreshToken = runBlocking { tokenDataStore.refreshToken.first() }
            if (refreshToken.isNullOrBlank()) {
                clearSession()
                return null
            }

            val newTokens =
                when (
                    val result =
                        runBlocking { refreshApi.refresh(RefreshRequest(RefreshRequest.Auth(refreshToken))) }
                ) {
                    is ChallaResult.Success -> result.data.data?.takeIf { result.data.success }?.auth
                    is ChallaResult.Failure -> null
                }

            if (newTokens == null) {
                clearSession()
                return null
            }

            runBlocking { tokenDataStore.saveTokens(newTokens.accessToken, newTokens.refreshToken) }
            return response.request.withBearer(newTokens.accessToken)
        }

        private fun clearSession() {
            runBlocking { tokenDataStore.clear() }
            userProfileCache.clear()
        }

        private fun Request.withBearer(accessToken: String): Request =
            newBuilder()
                .header(AUTHORIZATION, "$BEARER_PREFIX$accessToken")
                .build()

        private fun responseCount(response: Response): Int {
            var count = 1
            var prior = response.priorResponse
            while (prior != null) {
                count++
                prior = prior.priorResponse
            }
            return count
        }

        companion object {
            private const val AUTHORIZATION = "Authorization"
            private const val BEARER_PREFIX = "Bearer "
            private const val MAX_ATTEMPTS = 2
        }
    }
