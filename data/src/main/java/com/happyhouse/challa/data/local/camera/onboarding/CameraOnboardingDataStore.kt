package com.happyhouse.challa.data.local.camera.onboarding

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.happyhouse.challa.domain.result.ChallaResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.cameraOnboardingDataStore by preferencesDataStore(name = "camera_onboarding")

@Singleton
class CameraOnboardingDataStore
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val dataStore = context.cameraOnboardingDataStore

        val hasCompleted: Flow<ChallaResult<Boolean>> =
            dataStore.data
                .map<Preferences, ChallaResult<Boolean>> { preferences ->
                    ChallaResult.Success(
                        preferences[HAS_COMPLETED_KEY] ?: false,
                    )
                }.catch { throwable ->
                    if (throwable is CancellationException) throw throwable
                    emit(ChallaResult.Failure.Unknown(throwable))
                }

        suspend fun complete() {
            dataStore.edit { preferences ->
                preferences[HAS_COMPLETED_KEY] = true
            }
        }

        private companion object {
            val HAS_COMPLETED_KEY = booleanPreferencesKey("has_completed")
        }
    }
