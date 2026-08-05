package com.happyhouse.challa.data.network.api

import com.happyhouse.challa.data.network.dto.BaseResponse
import com.happyhouse.challa.data.network.dto.response.GetRoomResponse
import com.happyhouse.challa.domain.result.ChallaResult
import retrofit2.http.GET
import retrofit2.http.Path

interface RoomApi {
    @GET("api/v1/rooms/{roomId}")
    suspend fun getRoom(
        @Path("roomId") roomId: Long,
    ): ChallaResult<BaseResponse<GetRoomResponse>>
}
