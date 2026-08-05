package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.model.RoomDetail
import com.happyhouse.challa.domain.model.RoomSummary
import com.happyhouse.challa.domain.result.ChallaResult

interface RoomRepository {
    suspend fun getRoom(roomId: Long): ChallaResult<RoomDetail>

    suspend fun getRooms(): ChallaResult<List<RoomSummary>>
}
