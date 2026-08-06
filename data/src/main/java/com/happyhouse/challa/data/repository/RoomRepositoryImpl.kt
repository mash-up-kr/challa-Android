package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.network.api.RoomApi
import com.happyhouse.challa.data.network.dto.CreateRoomRequest
import com.happyhouse.challa.data.network.dto.JoinRoomRequest
import com.happyhouse.challa.domain.model.CreatedRoom
import com.happyhouse.challa.domain.model.RoomDetail
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.domain.model.RoomSummary
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject

class RoomRepositoryImpl
    @Inject
    constructor(
        private val roomApi: RoomApi,
    ) : RoomRepository {
        override suspend fun getRoom(roomId: Long): ChallaResult<RoomDetail> =
            roomApi.getRoom(roomId).mapCatching { response ->
                check(response.success) { response.message }
                val room = requireNotNull(response.data) { "방 상세 응답 데이터가 비어 있습니다." }.room
                RoomDetail(
                    // 응답의 id가 비어 있어도 요청에 쓴 방 번호는 확실하므로 그대로 쓴다.
                    id = room.id ?: roomId,
                    title = room.title,
                    totalPhotoCount = room.totalPhotoCount,
                    remainedPhotoCount = room.remainedPhotoCount,
                    invitationCode = room.invitationCode,
                    status = room.status.toRoomStatus(),
                    photoPrintCompletionAt = room.photoPrintCompletionAt?.toInstantOrNull(),
                )
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

        /**
         * 서버가 상태를 새로 추가해도 방 이름·사진까지 통째로 못 보게 되지는 않도록,
         * 모르는 상태는 촬영 중으로 본다.
         */
        private fun String.toRoomStatus(): RoomStatus =
            when (this) {
                SHOOTING -> RoomStatus.SHOOTING
                PHOTO_PRINT_PENDING -> RoomStatus.PHOTO_PRINT_PENDING
                PHOTO_PRINT_COMPLETED -> RoomStatus.PHOTO_PRINT_COMPLETED
                else -> RoomStatus.SHOOTING
            }

        /**
         * 서버가 내려주는 ISO-8601 시각을 파싱한다.
         *
         * 오프셋이 붙어 오면 그대로 쓰고, 없으면 UTC로 본다.
         * 해석하지 못한 시각은 null로 두어, 완료 시각을 모르는 상태로 화면이 판단하게 한다.
         */
        private fun String.toInstantOrNull(): Instant? =
            runCatching { OffsetDateTime.parse(this).toInstant() }
                .recoverCatching { LocalDateTime.parse(this).toInstant(ZoneOffset.UTC) }
                .getOrNull()

        companion object {
            private const val SHOOTING = "SHOOTING"
            private const val PHOTO_PRINT_PENDING = "PHOTO_PRINT_PENDING"
            private const val PHOTO_PRINT_COMPLETED = "PHOTO_PRINT_COMPLETED"
        }
    }
