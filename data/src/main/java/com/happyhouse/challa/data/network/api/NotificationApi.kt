package com.happyhouse.challa.data.network.api

import com.happyhouse.challa.data.network.dto.BaseResponse
import com.happyhouse.challa.data.network.dto.request.NotificationTokenRequest
import com.happyhouse.challa.domain.result.ChallaResult
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST

interface NotificationApi {
    @POST("api/v1/notifications/tokens")
    suspend fun registerToken(
        @Body request: NotificationTokenRequest,
    ): ChallaResult<BaseResponse<Unit>>

    @HTTP(
        method = "DELETE",
        path = "api/v1/notifications/tokens",
        hasBody = true,
    )
    suspend fun deleteToken(
        @Body request: NotificationTokenRequest,
    ): ChallaResult<BaseResponse<Unit>>
}
