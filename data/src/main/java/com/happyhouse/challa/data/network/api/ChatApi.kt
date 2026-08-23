package com.happyhouse.challa.data.network.api

import com.happyhouse.challa.data.network.dto.BaseResponse
import com.happyhouse.challa.data.network.dto.request.CreateChatRequest
import com.happyhouse.challa.data.network.dto.response.CreateChatResponse
import com.happyhouse.challa.data.network.dto.response.GetPhotoDetailResponse
import com.happyhouse.challa.domain.result.ChallaResult
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatApi {
    /**
     * 사진에 남기는 반응과 메시지가 함께 쓰는 엔드포인트다.
     * 방 전체 채팅은 `api/v1/chats`로 따로 나가 있다.
     */
    @POST("api/v1/chats/reaction")
    suspend fun postChatForReaction(
        @Body request: CreateChatRequest,
    ): ChallaResult<BaseResponse<CreateChatResponse>>

    @DELETE("api/v1/chats/reaction/{chatId}")
    suspend fun deleteChatForReaction(
        @Path("chatId") chatId: Long,
    ): ChallaResult<BaseResponse<Unit>>

    /** 사진 본문은 목록 API로 채우고, 이 응답에서는 사진에 달린 반응·댓글만 쓴다. */
    @GET("api/v1/photos/{photoId}")
    suspend fun getPhotoDetail(
        @Path("photoId") photoId: Long,
    ): ChallaResult<BaseResponse<GetPhotoDetailResponse>>
}
