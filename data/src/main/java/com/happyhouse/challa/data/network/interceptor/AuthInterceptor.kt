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
/**
 * 저장된 액세스 토큰을 `Authorization: Bearer` 헤더로 붙인다.
 *
 * 토큰이 없으면(로그인 전) 헤더 없이 그대로 진행한다. 로그인/토큰 갱신 요청은 이 인터셉터를
 * 거치지 않는 별도의 무인증 클라이언트(`@RefreshClient`)를 사용하므로 여기서 예외 처리를 두지 않는다.
 */
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
