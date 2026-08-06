package com.happyhouse.challa.data.network.api

import com.happyhouse.challa.data.network.dto.BaseResponse
import com.happyhouse.challa.data.network.dto.CreateRoomRequest
import com.happyhouse.challa.data.network.dto.CreateRoomResponse
import com.happyhouse.challa.data.network.dto.JoinRoomRequest
import com.happyhouse.challa.data.network.dto.JoinRoomResponse
import com.happyhouse.challa.data.network.dto.response.GetRoomResponse
import com.happyhouse.challa.domain.result.ChallaResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RoomApi {
    @GET("api/v1/rooms/{roomId}")
    suspend fun getRoom(
        @Path("roomId") roomId: Long,
    ): ChallaResult<BaseResponse<GetRoomResponse>>

    @POST("api/v1/rooms")
    suspend fun postRoom(
        @Body request: CreateRoomRequest,
    ): ChallaResult<BaseResponse<CreateRoomResponse>>

    @POST("api/v1/rooms/join")
    suspend fun joinRoom(
        @Body request: JoinRoomRequest,
    ): ChallaResult<BaseResponse<JoinRoomResponse>>
}
