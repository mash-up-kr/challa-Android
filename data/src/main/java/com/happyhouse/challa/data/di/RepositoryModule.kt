package com.happyhouse.challa.data.di

import com.happyhouse.challa.data.repository.AuthRepositoryImpl
import com.happyhouse.challa.data.repository.CameraRepositoryImpl
import com.happyhouse.challa.data.repository.ImageUploadRepositoryImpl
import com.happyhouse.challa.data.repository.NotificationRepositoryImpl
import com.happyhouse.challa.data.repository.PhotoRepositoryImpl
import com.happyhouse.challa.data.repository.RoomRepositoryImpl
import com.happyhouse.challa.data.repository.RoomVisitRepositoryImpl
import com.happyhouse.challa.data.repository.ThemeRepositoryImpl
import com.happyhouse.challa.data.repository.UserRepositoryImpl
import com.happyhouse.challa.domain.repository.AuthRepository
import com.happyhouse.challa.domain.repository.CameraRepository
import com.happyhouse.challa.domain.repository.ImageUploadRepository
import com.happyhouse.challa.domain.repository.NotificationRepository
import com.happyhouse.challa.domain.repository.PhotoRepository
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.repository.RoomVisitRepository
import com.happyhouse.challa.domain.repository.ThemeRepository
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
    abstract fun bindCameraRepository(impl: CameraRepositoryImpl): CameraRepository

    @Binds
    @Singleton
    abstract fun bindPhotoRepository(impl: PhotoRepositoryImpl): PhotoRepository

    @Binds
    @Singleton
    abstract fun bindRoomRepository(impl: RoomRepositoryImpl): RoomRepository

    @Binds
    @Singleton
    abstract fun bindRoomVisitRepository(impl: RoomVisitRepositoryImpl): RoomVisitRepository

    @Binds
    @Singleton
    abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindImageUploadRepository(impl: ImageUploadRepositoryImpl): ImageUploadRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository
}
