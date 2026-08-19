package com.happyhouse.challa.data.network.api

import com.happyhouse.challa.data.network.dto.BaseResponse
import com.happyhouse.challa.data.network.dto.request.CreateChatRequest
import com.happyhouse.challa.domain.result.ChallaResult
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApi {
    /**
     * 사진에 남기는 반응과 메시지가 함께 쓰는 엔드포인트다.
     * 방 전체 채팅은 `api/v1/chats`로 따로 나가 있다.
     *
     * 응답 본문은 보낸 내용을 그대로 돌려주기만 해서 쓰지 않는다.
     */
    @POST("api/v1/chats/reaction")
    suspend fun postChatForReaction(
        @Body request: CreateChatRequest,
    ): ChallaResult<BaseResponse<Unit>>
}
