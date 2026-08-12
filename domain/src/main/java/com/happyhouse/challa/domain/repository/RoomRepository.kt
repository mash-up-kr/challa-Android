package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.model.CreatedRoom
import com.happyhouse.challa.domain.model.RoomDetail
import com.happyhouse.challa.domain.model.RoomUser
import com.happyhouse.challa.domain.model.ShootableRoom
import com.happyhouse.challa.domain.result.ChallaResult

interface RoomRepository {
    suspend fun getRoom(roomId: Long): ChallaResult<RoomDetail>

    suspend fun getRoomUsers(roomId: Long): ChallaResult<List<RoomUser>>

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
}
