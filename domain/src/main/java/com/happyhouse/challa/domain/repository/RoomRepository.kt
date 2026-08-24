package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.model.CreatedRoom
import com.happyhouse.challa.domain.model.Room
import com.happyhouse.challa.domain.model.RoomDetail
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.domain.model.RoomUser
import com.happyhouse.challa.domain.model.ShootableRoom
import com.happyhouse.challa.domain.result.ChallaResult

interface RoomRepository {
    suspend fun getRoom(roomId: Long): ChallaResult<RoomDetail>

    suspend fun getRoomUsers(roomId: Long): ChallaResult<List<RoomUser>>

    suspend fun getRoomList(statuses: List<RoomStatus>): ChallaResult<List<Room>>

    suspend fun postRoom(
        title: String,
        totalPhotoCount: Int,
    ): ChallaResult<CreatedRoom>

    /**
     * 입장 코드로 방에 참여한다. 성공 시 참여한 방의 식별자를 반환한다.
     *
     * @param code 방 입장 코드.
     */
    suspend fun enterRoom(code: String): ChallaResult<CreatedRoom>

    suspend fun getShootableRooms(): ChallaResult<List<ShootableRoom>>

    /**
     * 인화 완료 연출을 끝까지 본 것을 서버에 기록한다.
     * 기록한 뒤에는 방 목록의 `photoPrintCompletionCheckedAt`가 채워져 연출이 다시 재생되지 않는다.
     */
    suspend fun checkPhotoPrintCompletion(roomId: Long): ChallaResult<Unit>
}
