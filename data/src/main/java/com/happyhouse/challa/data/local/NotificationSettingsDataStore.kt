package com.happyhouse.challa.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.happyhouse.challa.domain.result.ChallaResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.notificationSettingsDataStore by preferencesDataStore(name = "notification_settings")

@Singleton
class NotificationSettingsDataStore
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val dataStore = context.notificationSettingsDataStore

        val isEnabled: Flow<ChallaResult<Boolean>> =
            dataStore.data
                .map<Preferences, ChallaResult<Boolean>> { preferences ->
                    ChallaResult.Success(
                        preferences[SERVICE_NOTIFICATIONS_ENABLED_KEY] ?: true,
                    )
                }.catch { throwable ->
                    if (throwable is CancellationException) throw throwable
                    emit(ChallaResult.Failure.Unknown(throwable))
                }

        val pushToken: Flow<String?> =
            dataStore.data.map { preferences -> preferences[PUSH_TOKEN_KEY] }

        suspend fun setEnabled(enabled: Boolean) {
            dataStore.edit { preferences ->
                preferences[SERVICE_NOTIFICATIONS_ENABLED_KEY] = enabled
            }
        }

        suspend fun savePushToken(token: String) {
            dataStore.edit { preferences ->
                preferences[PUSH_TOKEN_KEY] = token
            }
        }

        private companion object {
            val SERVICE_NOTIFICATIONS_ENABLED_KEY =
                booleanPreferencesKey("notifications_enabled")
            val PUSH_TOKEN_KEY = stringPreferencesKey("push_token")
        }
    }
