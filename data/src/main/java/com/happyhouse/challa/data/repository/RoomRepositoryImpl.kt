package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.network.api.RoomApi
import com.happyhouse.challa.data.network.dto.CreateRoomRequest
import com.happyhouse.challa.data.network.dto.JoinRoomRequest
import com.happyhouse.challa.domain.model.CreatedRoom
import com.happyhouse.challa.domain.model.ShootableRoom
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
    private val roomApi: RoomApi,
) : RoomRepository {
    override suspend fun enterRoom(code: String): ChallaResult<CreatedRoom> =
        roomApi
            .joinRoom(
                JoinRoomRequest(
                    room =
                        JoinRoomRequest.Room(
                            invitationCode = code,
                        ),
                ),
            ).mapCatching { response ->
                check(response.success) { response.message }
                val data = requireNotNull(response.data) { "방 입장 응답 데이터가 비어 있습니다." }
                CreatedRoom(id = data.room.id)
            }

    override suspend fun postRoom(
        title: String,
        totalPhotoCount: Int,
    ): ChallaResult<CreatedRoom> =
        roomApi
            .postRoom(
                CreateRoomRequest(
                    room =
                        CreateRoomRequest.Room(
                            title = title,
                            totalPhotoCount = totalPhotoCount,
                        ),
                ),
            ).mapCatching { response ->
                check(response.success) { response.message }
                val data = requireNotNull(response.data) { "방 생성 응답 데이터가 비어 있습니다." }
                CreatedRoom(id = data.room.id)
            }

    override suspend fun getShootableRooms(): ChallaResult<List<ShootableRoom>> =
        roomApi.getShootableRooms().mapCatching { response ->
            check(response.success) { response.message }
            requireNotNull(response.data) { "촬영 가능한 방 응답 데이터가 비어 있습니다." }
                .room
                .map { room ->
                    ShootableRoom(
                        id = room.id,
                        title = room.title,
                        remainingCount = room.remainedPhotoCount,
                        totalCount = room.totalPhotoCount,
                    )
                }
        }
}
