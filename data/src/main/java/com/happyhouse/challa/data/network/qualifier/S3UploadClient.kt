package com.happyhouse.challa.data.network.qualifier

import javax.inject.Qualifier

/**
 * S3 서명 URL 로 이미지 바이너리를 직접 PUT 하는 전용 OkHttpClient 를 구분하는 한정자.
 *
 * 이 클라이언트는 [com.happyhouse.challa.data.network.interceptor.AuthInterceptor] 를 포함하지 않는다.
 * 서명 URL 요청에 `Authorization` 헤더가 붙으면 서명이 깨져 403 이 나므로, 인증 헤더 없이 전송해야 한다.
 * 또한 이미지 바이너리가 로그로 새는 것을 막기 위해 BODY 로깅 인터셉터도 달지 않는다.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class S3UploadClient
