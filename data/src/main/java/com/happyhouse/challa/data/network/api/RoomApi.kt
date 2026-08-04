package com.happyhouse.challa.data.network.api

import com.happyhouse.challa.data.network.dto.BaseResponse
import com.happyhouse.challa.data.network.dto.response.ShootableRoomResponse
import com.happyhouse.challa.domain.result.ChallaResult
import retrofit2.http.GET

interface RoomApi {
    @GET("api/v1/rooms/shootable")
    suspend fun getShootableRooms(): ChallaResult<BaseResponse<ShootableRoomResponse>>
}
