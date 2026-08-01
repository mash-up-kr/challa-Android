package com.happyhouse.challa.data.di

import com.happyhouse.challa.data.repository.AuthRepositoryImpl
import com.happyhouse.challa.data.repository.UserRepositoryImpl
import com.happyhouse.challa.domain.repository.AuthRepository
import com.happyhouse.challa.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
