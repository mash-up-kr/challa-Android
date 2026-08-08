package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.network.api.RoomApi
import com.happyhouse.challa.data.network.dto.CreateRoomRequest
import com.happyhouse.challa.data.network.dto.toDomain
import com.happyhouse.challa.domain.model.CreatedRoom
import com.happyhouse.challa.domain.model.Room
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.domain.model.RoomSummary
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import javax.inject.Inject

class RoomRepositoryImpl
    @Inject
    constructor(
        private val roomApi: RoomApi,
    ) : RoomRepository {
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

        override suspend fun getRooms(): ChallaResult<List<RoomSummary>> =
            ChallaResult.Success(
                listOf(
                    RoomSummary(
                        id = 1L,
                        name = "방이름1",
                        remainingCount = 24,
                        totalCount = 24,
                    ),
                    RoomSummary(
                        id = 2L,
                        name = "방이름방이름방이름2",
                        remainingCount = 6,
                        totalCount = 24,
                    ),
                    RoomSummary(
                        id = 3L,
                        name = "방이름방이름방이름3방이름",
                        remainingCount = 5,
                        totalCount = 48,
                    ),
                    RoomSummary(
                        id = 4L,
                        name = "방이름방이름방이름4",
                        remainingCount = 0,
                        totalCount = 48,
                    ),
                    RoomSummary(
                        id = 5L,
                        name = "방이름5",
                        remainingCount = 12,
                        totalCount = 24,
                    ),
                ),
            )

        override suspend fun getRoomList(statuses: List<RoomStatus>): ChallaResult<List<Room>> =
            roomApi
                .getRooms(statuses.filterNot { it == RoomStatus.UNKNOWN }.map { it.name })
                .mapCatching { response ->
                    check(response.success) { response.message }
                    val data = requireNotNull(response.data) { "방 목록 응답 데이터가 비어 있습니다." }
                    data.rooms.map { it.toDomain() }
                }
    }
