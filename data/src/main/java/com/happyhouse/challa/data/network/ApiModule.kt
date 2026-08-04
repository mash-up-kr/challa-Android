package com.happyhouse.challa.data.network

import com.happyhouse.challa.data.network.api.AuthApi
import com.happyhouse.challa.data.network.api.CameraApi
import com.happyhouse.challa.data.network.api.CameraFilterFileApi
import com.happyhouse.challa.data.network.api.UploadApi
import com.happyhouse.challa.data.network.api.UserApi
import com.happyhouse.challa.data.network.qualifier.CameraFilterClient
import com.happyhouse.challa.data.network.qualifier.RefreshClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create()

    @Provides
    @Singleton
    fun provideCameraApi(retrofit: Retrofit): CameraApi = retrofit.create()

    @Provides
    @Singleton
    fun provideCameraFilterFileApi(
        @CameraFilterClient retrofit: Retrofit,
    ): CameraFilterFileApi = retrofit.create()

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create()

    @Provides
    @Singleton
    fun provideUploadApi(retrofit: Retrofit): UploadApi = retrofit.create()

    @Provides
    @Singleton
    @RefreshClient
    fun provideRefreshAuthApi(
        @RefreshClient retrofit: Retrofit,
    ): AuthApi = retrofit.create()
}
