package com.happyhouse.challa.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.roomVisitDataStore by preferencesDataStore(name = "room_visit")

/**
 * 이미 들어가 본 방의 식별자를 Preferences DataStore에 영속화한다.
 *
 * 기록이 무한히 쌓이지 않도록 최근 [MAX_VISITED_ROOM_COUNT]개만 남기고 오래된 것부터 버린다.
 * 버려진 방에 다시 들어가면 첫 진입으로 보이지만, 그만큼 오래전에 들어간 방이라 안내를 다시 봐도 무방하다.
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
                val visitedRoomIds = preferences[VISITED_ROOM_IDS_KEY].toRoomIds()
                isFirstVisit = roomId.toString() !in visitedRoomIds
                if (isFirstVisit) {
                    // 오래 전에 들어간 방부터 밀어낸다.
                    preferences[VISITED_ROOM_IDS_KEY] =
                        (visitedRoomIds + roomId.toString())
                            .takeLast(MAX_VISITED_ROOM_COUNT)
                            .joinToString(ROOM_ID_DELIMITER)
                }
            }

            return isFirstVisit
        }

        private fun String?.toRoomIds(): List<String> =
            this
                ?.split(ROOM_ID_DELIMITER)
                ?.filter { roomId -> roomId.isNotBlank() }
                .orEmpty()

        private companion object {
            /** 들어간 순서를 알아야 오래된 것부터 지울 수 있어 Set 대신 순서가 있는 문자열로 담는다. */
            val VISITED_ROOM_IDS_KEY = stringPreferencesKey("recent_visited_room_ids")

            const val ROOM_ID_DELIMITER = ","
            const val MAX_VISITED_ROOM_COUNT = 100
        }
    }
