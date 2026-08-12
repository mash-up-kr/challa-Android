package com.happyhouse.challa.data.network.api

import com.happyhouse.challa.data.network.dto.BaseResponse
import com.happyhouse.challa.data.network.dto.response.ListPhotosResponse
import com.happyhouse.challa.domain.result.ChallaResult
import retrofit2.http.GET
import retrofit2.http.Query

interface PhotoApi {
    /** @param page 0부터 시작한다. */
    @GET("api/v1/photos")
    suspend fun getPhotos(
        @Query("roomId") roomId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): ChallaResult<BaseResponse<ListPhotosResponse>>
}
