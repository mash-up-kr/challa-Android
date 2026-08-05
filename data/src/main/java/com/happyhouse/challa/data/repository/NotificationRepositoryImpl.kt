package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.local.NotificationSettingsDataStore
import com.happyhouse.challa.domain.repository.NotificationRepository
import com.happyhouse.challa.domain.result.ChallaResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl
    @Inject
    constructor(
        private val notificationSettingsDataStore: NotificationSettingsDataStore,
    ) : NotificationRepository {
        override val isEnabled: Flow<ChallaResult<Boolean>> = notificationSettingsDataStore.isEnabled

        override suspend fun setEnabled(enabled: Boolean): ChallaResult<Unit> =
            try {
                notificationSettingsDataStore.setEnabled(enabled)
                ChallaResult.Success(Unit)
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                ChallaResult.Failure.Unknown(throwable)
            }
    }
