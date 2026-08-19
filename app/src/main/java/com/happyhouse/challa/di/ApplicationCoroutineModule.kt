package com.happyhouse.challa.di

import com.happyhouse.challa.di.qualifier.ApplicationScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationCoroutineModule {
    /** 앱 프로세스 동안 유지되며 하위 작업 하나의 실패가 다른 작업을 취소하지 않는 scope입니다. */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
