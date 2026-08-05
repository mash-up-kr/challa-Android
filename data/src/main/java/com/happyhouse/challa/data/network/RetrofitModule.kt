package com.happyhouse.challa.data.network

import com.happyhouse.challa.data.BuildConfig
import com.happyhouse.challa.data.network.adapter.ChallaResultCallAdapterFactory
import com.happyhouse.challa.data.network.qualifier.CameraFilterClient
import com.happyhouse.challa.data.network.qualifier.RefreshClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {
    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit =
        createRetrofitBuilder(okHttpClient)
            .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE.toMediaType()))
            .build()

    @Provides
    @Singleton
    @CameraFilterClient
    fun provideCameraFilterRetrofit(
        @CameraFilterClient okHttpClient: OkHttpClient,
    ): Retrofit = createRetrofitBuilder(okHttpClient).build()

    @Provides
    @Singleton
    @RefreshClient
    fun provideRefreshRetrofit(
        @RefreshClient okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit =
        createRetrofitBuilder(okHttpClient)
            .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE.toMediaType()))
            .build()

    private fun createRetrofitBuilder(okHttpClient: OkHttpClient): Retrofit.Builder =
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(ChallaResultCallAdapterFactory())

    private const val JSON_MEDIA_TYPE = "application/json"
}
