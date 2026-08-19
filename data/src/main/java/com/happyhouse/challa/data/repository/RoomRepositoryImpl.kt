package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.network.api.RoomApi
import com.happyhouse.challa.data.network.dto.CreateRoomRequest
import com.happyhouse.challa.data.network.dto.JoinRoomRequest
import com.happyhouse.challa.data.network.dto.response.GetRoomResponse
import com.happyhouse.challa.data.network.dto.toDomain
import com.happyhouse.challa.data.network.parseServerInstant
import com.happyhouse.challa.domain.model.CreatedRoom
import com.happyhouse.challa.domain.model.Room
import com.happyhouse.challa.domain.model.RoomDetail
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.domain.model.RoomUser
import com.happyhouse.challa.domain.model.ShootableRoom
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
    private val roomApi: RoomApi,
) : RoomRepository {
    override suspend fun getRoom(roomId: Long): ChallaResult<RoomDetail> =
        roomApi.getRoom(roomId).mapCatching { response ->
            check(response.success) { response.message }
            val room = requireNotNull(response.data) { "방 상세 응답 데이터가 비어 있습니다." }.room
            RoomDetail(
                id = room.id,
                title = room.title,
                totalPhotoCount = room.totalPhotoCount,
                remainedPhotoCount = room.remainedPhotoCount,
                invitationCode = room.invitationCode,
                status = room.status.toRoomStatus(),
                photoPrintCompletedAt = room.photoPrintCompletedAt?.parseServerInstant(),
            )
        }

    override suspend fun getRoomUsers(roomId: Long): ChallaResult<List<RoomUser>> =
        roomApi.getRoomUsers(roomId).mapCatching { response ->
            check(response.success) { response.message }
            val users = requireNotNull(response.data) { "방 참여자 응답 데이터가 비어 있습니다." }.users
            users.map { user ->
                RoomUser(
                    id = user.id,
                    nickname = user.nickname,
                    profileImageUrl = user.profileImageUrl,
                )
            }
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

    override suspend fun getShootableRooms(): ChallaResult<List<ShootableRoom>> =
        roomApi.getShootableRooms().mapCatching { response ->
            check(response.success) { response.message }
            requireNotNull(response.data) { "촬영 가능한 방 응답 데이터가 비어 있습니다." }
                .rooms
                .map { room ->
                    ShootableRoom(
                        id = room.id,
                        title = room.title,
                        remainingCount = room.remainedPhotoCount,
                        totalCount = room.totalPhotoCount,
                    )
                }
        }

    override suspend fun getRoomList(statuses: List<RoomStatus>): ChallaResult<List<Room>> =
        roomApi
            .getRooms(statuses.filterNot { it == RoomStatus.UNKNOWN }.map { it.name })
            .mapCatching { response ->
                check(response.success) { response.message }
                val data = requireNotNull(response.data) { "방 목록 응답 데이터가 비어 있습니다." }
                data.rooms.map { it.toDomain() }
            }

    private fun GetRoomResponse.Status.toRoomStatus(): RoomStatus =
        when (this) {
            GetRoomResponse.Status.SHOOTING -> RoomStatus.SHOOTING
            GetRoomResponse.Status.PHOTO_PRINT_PENDING -> RoomStatus.PHOTO_PRINT_PENDING
            GetRoomResponse.Status.PHOTO_PRINT_COMPLETED -> RoomStatus.PHOTO_PRINT_COMPLETED
            GetRoomResponse.Status.UNKNOWN -> RoomStatus.UNKNOWN
        }
}
