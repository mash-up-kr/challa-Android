package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.model.CreatedRoom
import com.happyhouse.challa.domain.model.Room
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.domain.model.RoomSummary
import com.happyhouse.challa.domain.result.ChallaResult

interface RoomRepository {
    suspend fun getRooms(): ChallaResult<List<RoomSummary>>

    suspend fun getRoomList(statuses: List<RoomStatus>): ChallaResult<List<Room>>

    suspend fun postRoom(
        title: String,
        totalPhotoCount: Int,
    ): ChallaResult<CreatedRoom>
}
