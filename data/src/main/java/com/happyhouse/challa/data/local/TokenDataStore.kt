package com.happyhouse.challa.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 로그인 토큰(access/refresh)을 DataStore 에 보관한다.
 *
 * 인증 헤더 부착·자동 갱신 등 토큰을 실제로 쓰는 쪽은 이 저장소를 통해 토큰을 읽는다.
 */
@Singleton
class TokenDataStore
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        val accessToken: Flow<String?> = dataStore.data.map { it[ACCESS_TOKEN_KEY] }
        val refreshToken: Flow<String?> = dataStore.data.map { it[REFRESH_TOKEN_KEY] }

        suspend fun saveTokens(
            accessToken: String,
            refreshToken: String,
        ) {
            dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN_KEY] = accessToken
                preferences[REFRESH_TOKEN_KEY] = refreshToken
            }
        }

        suspend fun clear() {
            dataStore.edit { preferences ->
                preferences.remove(ACCESS_TOKEN_KEY)
                preferences.remove(REFRESH_TOKEN_KEY)
            }
        }

        companion object {
            private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
            private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        }
    }
