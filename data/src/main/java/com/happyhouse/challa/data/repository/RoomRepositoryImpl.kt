package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.network.api.RoomApi
import com.happyhouse.challa.domain.model.RoomSummary
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepositoryImpl @Inject constructor(
    private val roomApi: RoomApi,
) : RoomRepository {
    override suspend fun getRooms(): ChallaResult<List<RoomSummary>> =
        roomApi.getShootableRooms().mapCatching { response ->
            check(response.success) { response.message }
            requireNotNull(response.data) { "촬영 가능한 방 응답 데이터가 비어 있습니다." }
                .room
                .map { room ->
                    RoomSummary(
                        id = room.id,
                        name = room.title,
                        remainingCount = room.remainedPhotoCount,
                        totalCount = room.totalPhotoCount,
                    )
                }
        }
}
