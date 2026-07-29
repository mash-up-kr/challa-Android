package com.happyhouse.challa.data.network.qualifier

import javax.inject.Qualifier

/**
 * 토큰 재발급 전용 네트워크 컴포넌트(OkHttpClient/Retrofit/AuthApi)를 구분하는 한정자.
 *
 * 이 클라이언트는 [com.happyhouse.challa.data.network.interceptor.TokenAuthenticator] 와
 * [com.happyhouse.challa.data.network.interceptor.AuthInterceptor] 를 포함하지 않는다.
 * (재발급 요청이 다시 401 → 재발급으로 이어지는 무한 루프·데드락 방지)
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RefreshClient
