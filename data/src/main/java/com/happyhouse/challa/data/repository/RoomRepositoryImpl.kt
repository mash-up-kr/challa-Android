package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.network.api.RoomApi
import com.happyhouse.challa.data.network.api.RoomWebSocketApi
import com.happyhouse.challa.data.network.dto.CreateRoomRequest
import com.happyhouse.challa.data.network.dto.JoinRoomRequest
import com.happyhouse.challa.data.network.dto.request.UpdateRoomTitleRequest
import com.happyhouse.challa.data.network.dto.response.GetRoomResponse
import com.happyhouse.challa.data.network.dto.toDomain
import com.happyhouse.challa.data.network.parseServerInstant
import com.happyhouse.challa.domain.event.RoomEvent
import com.happyhouse.challa.domain.model.CreatedRoom
import com.happyhouse.challa.domain.model.Room
import com.happyhouse.challa.domain.model.RoomDetail
import com.happyhouse.challa.domain.model.RoomMemberJoinedEvent
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.domain.model.RoomUser
import com.happyhouse.challa.domain.model.ShootableRoom
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import com.happyhouse.challa.domain.result.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
    private val roomApi: RoomApi,
    private val roomWebSocketApi: RoomWebSocketApi,
) : RoomRepository {
    override fun observeMemberJoined(roomIds: Set<Long>): Flow<RoomMemberJoinedEvent> =
        roomWebSocketApi.observeMemberJoined(roomIds).map { room ->
            RoomMemberJoinedEvent(
                roomId = room.id,
                roomTitle = room.title,
                nickname = room.userNickname,
                userProfileImageUrl = room.userProfileImageUrl,
            )
        }

    /**
     * 구독자가 값을 받아가기 전에 다음 이벤트가 올라와도 요청이 멈추지 않도록 한 칸을 둔다.
     * 지난 이벤트를 다시 줄 이유는 없으므로 replay는 두지 않는다.
     */
    private val _roomEventFlow = MutableSharedFlow<RoomEvent>(extraBufferCapacity = 1)
    override val roomEventFlow: Flow<RoomEvent> = _roomEventFlow.asSharedFlow()

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

    override suspend fun updateRoomTitle(
        roomId: Long,
        title: String,
    ): ChallaResult<Unit> =
        roomApi
            .putRoomTitle(
                roomId = roomId,
                request =
                    UpdateRoomTitleRequest(
                        room =
                            UpdateRoomTitleRequest.Room(
                                title = title,
                            ),
                    ),
            ).mapCatching { response ->
                check(response.success) { response.message }
            }.onSuccess {
                // 저장에 성공한 이름만 알린다. 실패한 이름이 다른 화면에 남으면 서버와 어긋난다.
                _roomEventFlow.emit(RoomEvent.TitleUpdate(roomId = roomId, title = title))
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

    override suspend fun checkPhotoPrintCompletion(roomId: Long): ChallaResult<Unit> =
        roomApi
            .checkPhotoPrintCompletion(roomId)
            .mapCatching { response ->
                check(response.success) { response.message }
            }.onSuccess {
                // 홈이 이 기록보다 먼저 방 목록을 받아오면 확인 전으로 남아 연출이 다시 재생된다.
                _roomEventFlow.emit(RoomEvent.PhotoPrintCompletionCheck(roomId = roomId))
            }

    private fun GetRoomResponse.Status.toRoomStatus(): RoomStatus =
        when (this) {
            GetRoomResponse.Status.SHOOTING -> RoomStatus.SHOOTING
            GetRoomResponse.Status.PHOTO_PRINT_PENDING -> RoomStatus.PHOTO_PRINT_PENDING
            GetRoomResponse.Status.PHOTO_PRINT_COMPLETED -> RoomStatus.PHOTO_PRINT_COMPLETED
            GetRoomResponse.Status.UNKNOWN -> RoomStatus.UNKNOWN
        }
}
