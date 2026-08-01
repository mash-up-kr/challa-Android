package com.happyhouse.challa.data.network.interceptor

import com.happyhouse.challa.data.local.TokenDataStore
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
 * 401(Unauthorized) 응답을 받으면 저장된 리프레시 토큰으로 새 토큰을 발급받아 원래 요청을 재시도한다.
 *
 * 재발급 호출은 인증 인터셉터·본 Authenticator 를 거치지 않는 [RefreshClient] 전용 [AuthApi] 로 보내
 * 무한 루프와 데드락을 피한다. 재발급까지 실패하면 저장된 토큰을 지우고 재시도를 포기한다(null 반환 → 401 그대로 전달).
 */
class TokenAuthenticator
    @Inject
    constructor(
        private val tokenDataStore: TokenDataStore,
        @RefreshClient private val refreshApi: AuthApi,
    ) : Authenticator {
        @Synchronized
        override fun authenticate(
            route: Route?,
            response: Response,
        ): Request? {
            // 새 토큰으로도 계속 401 이면(서버가 재발급 토큰을 거부) 무한 재시도를 막는다.
            if (responseCount(response) >= MAX_ATTEMPTS) return null

            val failedToken = response.request.header(AUTHORIZATION)?.removePrefix(BEARER_PREFIX)

            // 다른 요청이 이미 토큰을 갱신해 두었다면, 재발급 없이 갱신된 토큰으로 바로 재시도한다.
            val storedAccess = runBlocking { tokenDataStore.accessToken.first() }
            if (!storedAccess.isNullOrBlank() && storedAccess != failedToken) {
                return response.request.withBearer(storedAccess)
            }

            val refreshToken = runBlocking { tokenDataStore.refreshToken.first() }
            if (refreshToken.isNullOrBlank()) return null

            val newTokens =
                when (val result = runBlocking { refreshApi.refresh(RefreshRequest(refreshToken)) }) {
                    is ChallaResult.Success -> result.data.data?.takeIf { result.data.success }
                    is ChallaResult.Failure -> null
                }

            if (newTokens == null) {
                runBlocking { tokenDataStore.clear() }
                return null
            }

            runBlocking { tokenDataStore.saveTokens(newTokens.accessToken, newTokens.refreshToken) }
            return response.request.withBearer(newTokens.accessToken)
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
