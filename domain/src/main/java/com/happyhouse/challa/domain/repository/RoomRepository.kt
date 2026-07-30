package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.model.RoomSummary
import com.happyhouse.challa.domain.result.ChallaResult

interface RoomRepository {
    suspend fun getRooms(): ChallaResult<List<RoomSummary>>
}
