package com.happyhouse.challa.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.happyhouse.challa.domain.model.PrimaryTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore by preferencesDataStore(name = "theme")

/**
 * 사용자가 선택한 [PrimaryTheme]를 Preferences DataStore에 영속화합니다.
 *
 * 저장값이 없거나 알 수 없는 값이면 [PrimaryTheme.LEMONADE]를 기본값으로 사용하고,
 * 읽기 중 [IOException]이 발생해도 기본값을 방출해 앱 시작을 계속합니다.
 */
@Singleton
class ThemeDataStore
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val dataStore = context.themeDataStore

        val primaryTheme: Flow<PrimaryTheme> =
            dataStore.data
                .catch { throwable ->
                    if (throwable is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw throwable
                    }
                }.map { preferences ->
                    preferences[PRIMARY_THEME_KEY]
                        ?.let { savedTheme ->
                            PrimaryTheme.entries.firstOrNull { it.name == savedTheme }
                        } ?: PrimaryTheme.LEMONADE
                }

        suspend fun updatePrimaryTheme(theme: PrimaryTheme) {
            dataStore.edit { preferences ->
                preferences[PRIMARY_THEME_KEY] = theme.name
            }
        }

        private companion object {
            val PRIMARY_THEME_KEY = stringPreferencesKey("primary_theme")
        }
    }
