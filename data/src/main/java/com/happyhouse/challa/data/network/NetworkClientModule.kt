package com.happyhouse.challa.data.network

import android.content.Context
import com.happyhouse.challa.data.BuildConfig
import com.happyhouse.challa.data.FlavorExtraFunction
import com.happyhouse.challa.data.network.interceptor.AuthInterceptor
import com.happyhouse.challa.data.network.interceptor.TokenAuthenticator
import com.happyhouse.challa.data.network.qualifier.CameraFilterClient
import com.happyhouse.challa.data.network.qualifier.RefreshClient
import com.happyhouse.challa.data.network.qualifier.S3UploadClient
import com.orhanobut.logger.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkClientModule {
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor(
            NetworkLogPrinter(
                printLog = { Logger.t(NETWORK_LOG_TAG).d(it) },
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

    /**
     * 서버가 제공한 URL에서 `.cube` 필터 파일을 내려받는 전용 클라이언트.
     * 인증·로깅 인터셉터를 포함하지 않으며, 캐시 가능한 GET 응답은 앱 캐시 디렉터리에 저장해
     * 동일한 필터를 다시 요청할 때 재사용한다.
     *
     * [CAMERA_FILTER_CACHE_MAX_SIZE_BYTES]는 전체 디스크 캐시의 최대 용량이며,
     * 개별 필터 파일의 다운로드 크기를 제한하지 않는다.
     */
    @Provides
    @Singleton
    @CameraFilterClient
    fun provideCameraFilterOkHttpClient(
        @ApplicationContext context: Context,
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .cache(
                Cache(
                    directory = context.cacheDir.resolve(CAMERA_FILTER_CACHE_DIRECTORY),
                    maxSize = CAMERA_FILTER_CACHE_MAX_SIZE_BYTES,
                ),
            ).build()

    /**
     * S3 서명 URL 로 이미지를 직접 PUT 하는 전용 클라이언트. [AuthInterceptor] 를 달지 않아
     * `Authorization` 헤더 없이 전송하며, 이미지 바이너리가 logcat 으로 새지 않도록
     * [HttpLoggingInterceptor] 는 달지 않는다. 디버깅 편의를 위해 debug 빌드에서만 활성화되는
     * Flipper 인터셉터는 붙여, 업로드 요청의 URL·헤더·응답을 Flipper 에서 확인할 수 있게 한다.
     */
    @Provides
    @Singleton
    @S3UploadClient
    fun provideS3UploadOkHttpClient(flipperInterceptor: Interceptor?): OkHttpClient =
        OkHttpClient
            .Builder()
            .apply {
                flipperInterceptor?.let(::addNetworkInterceptor)
            }
            .connectTimeout(S3_UPLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(S3_UPLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(S3_UPLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

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

    private const val REFRESH_TIMEOUT_SECONDS = 10L
    private const val S3_UPLOAD_TIMEOUT_SECONDS = 30L
    private const val NETWORK_LOG_TAG = "OkHttp"
    private const val AUTHORIZATION_HEADER = "Authorization"
    private const val CAMERA_FILTER_CACHE_DIRECTORY = "camera_filters"
    private const val CAMERA_FILTER_CACHE_MAX_SIZE_BYTES = 20L * 1024L * 1024L
}
