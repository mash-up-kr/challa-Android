package com.happyhouse.challa.di

import com.happyhouse.challa.logging.FirebaseCrashReporter
import com.happyhouse.challa.presentation.logging.CrashReporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CrashReportingModule {
    @Binds
    @Singleton
    abstract fun bindCrashReporter(impl: FirebaseCrashReporter): CrashReporter
}
