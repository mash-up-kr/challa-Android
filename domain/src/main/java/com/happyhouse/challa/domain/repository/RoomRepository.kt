package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.event.RoomEvent
import com.happyhouse.challa.domain.model.CreatedRoom
import com.happyhouse.challa.domain.model.Room
import com.happyhouse.challa.domain.model.RoomDetail
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.domain.model.RoomUser
import com.happyhouse.challa.domain.model.ShootableRoom
import com.happyhouse.challa.domain.result.ChallaResult
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    /**
     * 방에 생긴 변화를 알리는 이벤트 흐름.
     *
     * 한 화면에서 바꾼 내용을 같은 방을 그리는 다른 화면들이 다시 조회하지 않고 반영하기 위해 쓴다.
     * 구독하는 쪽은 필요한 [RoomEvent] 하위 타입과 [RoomEvent.roomId]만 걸러 쓰면 되고,
     * 지금 구독 중인 화면에만 전달되며 지난 이벤트는 다시 주지 않는다.
     */
    val roomEventFlow: Flow<RoomEvent>

    suspend fun getRoom(roomId: Long): ChallaResult<RoomDetail>

    suspend fun getRoomUsers(roomId: Long): ChallaResult<List<RoomUser>>

    suspend fun getRoomList(statuses: List<RoomStatus>): ChallaResult<List<Room>>

    suspend fun postRoom(
        title: String,
        totalPhotoCount: Int,
    ): ChallaResult<CreatedRoom>

    /**
     * 방 이름을 바꾼다.
     *
     * @param title 새 방 이름.
     */
    suspend fun updateRoomTitle(
        roomId: Long,
        title: String,
    ): ChallaResult<Unit>

    /**
     * 입장 코드로 방에 참여한다. 성공 시 참여한 방의 식별자를 반환한다.
     *
     * @param code 방 입장 코드.
     */
    suspend fun enterRoom(code: String): ChallaResult<CreatedRoom>

    suspend fun getShootableRooms(): ChallaResult<List<ShootableRoom>>
}
