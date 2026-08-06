package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.model.CreatedRoom
import com.happyhouse.challa.domain.model.ShootableRoom
import com.happyhouse.challa.domain.result.ChallaResult

interface RoomRepository {
    suspend fun postRoom(
        title: String,
        totalPhotoCount: Int,
    ): ChallaResult<CreatedRoom>

    suspend fun getShootableRooms(): ChallaResult<List<ShootableRoom>>
}
