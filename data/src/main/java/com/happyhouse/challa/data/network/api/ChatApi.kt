package com.happyhouse.challa.data.network.api

import com.happyhouse.challa.data.network.dto.BaseResponse
import com.happyhouse.challa.data.network.dto.response.ChatsResponse
import com.happyhouse.challa.domain.result.ChallaResult
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApi {
    @GET("api/v1/chats/{roomId}")
    suspend fun getChats(
        @Path("roomId") roomId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): ChallaResult<BaseResponse<ChatsResponse>>
}
