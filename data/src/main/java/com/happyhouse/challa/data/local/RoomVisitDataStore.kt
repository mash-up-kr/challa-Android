package com.happyhouse.challa.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.roomVisitDataStore by preferencesDataStore(name = "room_visit")

/**
 * 이미 들어가 본 방의 식별자를 Preferences DataStore에 영속화한다.
 *
 * 기록이 계속 쌓이지만 한 사용자가 들어가는 방이 많지 않아 따로 지우지 않는다.
 */
@Singleton
class RoomVisitDataStore
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val dataStore = context.roomVisitDataStore

        /** 따로 읽고 쓰면 동시에 두 번 들어왔을 때 둘 다 첫 진입이 되므로 한 트랜잭션으로 처리한다. */
        suspend fun markVisited(roomId: Long): Boolean {
            var isFirstVisit = false

            dataStore.edit { preferences ->
                val visitedRoomIds = preferences[VISITED_ROOM_IDS_KEY].orEmpty()
                isFirstVisit = roomId.toString() !in visitedRoomIds
                if (isFirstVisit) {
                    preferences[VISITED_ROOM_IDS_KEY] = visitedRoomIds + roomId.toString()
                }
            }

            return isFirstVisit
        }

        private companion object {
            val VISITED_ROOM_IDS_KEY = stringSetPreferencesKey("visited_room_ids")
        }
    }
