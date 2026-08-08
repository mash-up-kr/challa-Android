package com.happyhouse.challa.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.happyhouse.challa.domain.model.PrimaryTheme
import com.happyhouse.challa.domain.result.ChallaResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore by preferencesDataStore(name = "theme")

/**
 * 사용자가 선택한 [PrimaryTheme]를 Preferences DataStore에 영속화합니다.
 *
 * 저장값이 없거나 알 수 없는 값이면 [PrimaryTheme.LEMONADE]를 기본값으로 사용하고,
 * 읽기 중 [IOException]이 발생하면 제한된 횟수만큼 재시도하고, 이후에는 실패 결과를 방출합니다.
 */
@Singleton
class ThemeDataStore
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val dataStore = context.themeDataStore
        private val themeReadRequests =
            MutableSharedFlow<Unit>(replay = 1).apply {
                tryEmit(Unit)
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        val primaryTheme: Flow<ChallaResult<PrimaryTheme>> =
            themeReadRequests.flatMapLatest {
                readPrimaryTheme()
            }

        private fun readPrimaryTheme(): Flow<ChallaResult<PrimaryTheme>> =
            dataStore.data
                .retryWhen { throwable, attempt ->
                    if (throwable !is IOException || attempt >= MAX_THEME_READ_RETRY_COUNT) {
                        return@retryWhen false
                    }

                    delay(THEME_READ_RETRY_DELAY_MILLIS * (attempt + 1))
                    true
                }.map { preferences -> preferences.toPrimaryThemeResult(PRIMARY_THEME_KEY) }
                .catch { throwable ->
                    if (throwable is CancellationException) throw throwable
                    emit(ChallaResult.Failure.Unknown(throwable))
                }

        suspend fun updatePrimaryTheme(theme: PrimaryTheme) {
            dataStore.edit { preferences ->
                preferences[PRIMARY_THEME_KEY] = theme.name
            }
            retryPrimaryThemeRead()
        }

        suspend fun clearPrimaryTheme() {
            dataStore.edit { preferences ->
                preferences.remove(PRIMARY_THEME_KEY)
            }
            retryPrimaryThemeRead()
        }

        fun retryPrimaryThemeRead() {
            themeReadRequests.tryEmit(Unit)
        }

        private companion object {
            const val MAX_THEME_READ_RETRY_COUNT = 3L
            const val THEME_READ_RETRY_DELAY_MILLIS = 1_000L
            val PRIMARY_THEME_KEY = stringPreferencesKey("primary_theme")
        }
    }

internal fun Preferences.toPrimaryThemeResult(primaryThemeKey: Preferences.Key<String>): ChallaResult<PrimaryTheme> =
    ChallaResult.Success(
        this[primaryThemeKey]
            ?.let { savedTheme ->
                PrimaryTheme.entries.firstOrNull { it.name == savedTheme }
            } ?: PrimaryTheme.LEMONADE,
    )
