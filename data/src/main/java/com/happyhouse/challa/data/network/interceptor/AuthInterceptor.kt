package com.happyhouse.challa.data.network.interceptor

import com.happyhouse.challa.data.local.TokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * 저장된 액세스 토큰을 `Authorization: Bearer` 헤더로 붙인다.
 *
 * 토큰이 없으면(로그인 전) 헤더 없이 그대로 진행한다. 로그인/토큰 갱신처럼 인증이 필요 없는 요청은
 * 이미 헤더가 있어도 서버가 무시하므로 별도 예외 처리는 두지 않는다.
 */
class AuthInterceptor
    @Inject
    constructor(
        private val tokenDataStore: TokenDataStore,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val accessToken = runBlocking { tokenDataStore.accessToken.first() }
            val request =
                chain
                    .request()
                    .newBuilder()
                    .apply {
                        if (!accessToken.isNullOrBlank()) {
                            header("Authorization", "Bearer $accessToken")
                        }
                    }.build()
            return chain.proceed(request)
        }
    }
