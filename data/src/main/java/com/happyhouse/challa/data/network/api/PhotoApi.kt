package com.happyhouse.challa.data.network.api

import com.happyhouse.challa.data.network.dto.BaseResponse
import com.happyhouse.challa.data.network.dto.CreatePhotoRequest
import com.happyhouse.challa.domain.result.ChallaResult
import retrofit2.http.Body
import retrofit2.http.POST

interface PhotoApi {
    @POST("api/v1/photos")
    suspend fun postPhoto(
        @Body request: CreatePhotoRequest,
    ): ChallaResult<BaseResponse<Unit>>
}
