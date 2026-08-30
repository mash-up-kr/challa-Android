package com.happyhouse.challa.data.network.api

import com.happyhouse.challa.data.network.dto.BaseResponse
import com.happyhouse.challa.data.network.dto.request.CreateChatRequest
import com.happyhouse.challa.data.network.dto.request.SendChatRequest
import com.happyhouse.challa.data.network.dto.response.ChatsResponse
import com.happyhouse.challa.data.network.dto.response.CreateChatResponse
import com.happyhouse.challa.domain.result.ChallaResult
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApi {
    @GET("api/v1/chats/{roomId}")
    suspend fun getChats(
        @Path("roomId") roomId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): ChallaResult<BaseResponse<ChatsResponse>>

    @POST("api/v1/chats")
    suspend fun postChat(
        @Body request: SendChatRequest,
    ): ChallaResult<BaseResponse<Unit>>

    /**
     * 사진에 남기는 반응과 메시지가 함께 쓰는 엔드포인트다.
     * 방 전체 채팅은 `api/v1/chats`로 따로 나가 있다.
     */
    @POST("api/v1/chats/reaction")
    suspend fun postPhotoChat(
        @Body request: CreateChatRequest,
    ): ChallaResult<BaseResponse<CreateChatResponse>>

    @DELETE("api/v1/chats/reaction/{chatId}")
    suspend fun deletePhotoReaction(
        @Path("chatId") chatId: Long,
    ): ChallaResult<BaseResponse<Unit>>
}
