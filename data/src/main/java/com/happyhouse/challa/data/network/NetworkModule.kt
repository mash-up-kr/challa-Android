package com.happyhouse.challa.data.network

import com.happyhouse.challa.data.BuildConfig
import com.happyhouse.challa.data.FlavorExtraFunction
import com.happyhouse.challa.data.network.adapter.ChallaResultCallAdapterFactory
import com.happyhouse.challa.data.network.api.AuthApi
import com.happyhouse.challa.data.network.interceptor.AuthInterceptor
import com.happyhouse.challa.data.network.interceptor.TokenAuthenticator
import com.happyhouse.challa.data.network.qualifier.RefreshClient
import com.orhanobut.logger.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor(
            NetworkLogPrinter(
                logText = { Logger.t(NETWORK_LOG_TAG).d(it) },
                logJson = { Logger.t(NETWORK_LOG_TAG).json(it) },
            ),
        ).apply {
            level =
                if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            redactHeader(AUTHORIZATION_HEADER)
        }

    @Provides
    @Singleton
    fun provideFlipperInterceptor(flavorExtraFunction: FlavorExtraFunction): Interceptor? = flavorExtraFunction.getFlipperInterceptor()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        flipperInterceptor: Interceptor?,
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .apply {
                flipperInterceptor?.let(::addNetworkInterceptor)
            }
            .authenticator(tokenAuthenticator)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(ChallaResultCallAdapterFactory())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    /**
     * 토큰 재발급 전용 클라이언트. [TokenAuthenticator]·[AuthInterceptor] 를 달지 않아
     * 재발급 요청이 다시 401 갱신 로직을 타지 않는다.
     */
    @Provides
    @Singleton
    @RefreshClient
    fun provideRefreshOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(httpLoggingInterceptor)
            .connectTimeout(REFRESH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(REFRESH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(REFRESH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @RefreshClient
    fun provideRefreshRetrofit(
        @RefreshClient okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(ChallaResultCallAdapterFactory())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    @RefreshClient
    fun provideRefreshAuthApi(
        @RefreshClient retrofit: Retrofit,
    ): AuthApi = retrofit.create(AuthApi::class.java)

    private const val REFRESH_TIMEOUT_SECONDS = 10L
    private const val NETWORK_LOG_TAG = "OkHttp"
    private const val AUTHORIZATION_HEADER = "Authorization"
}
