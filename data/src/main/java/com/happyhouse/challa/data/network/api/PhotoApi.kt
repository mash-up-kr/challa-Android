package com.happyhouse.challa.data.network.api

import com.happyhouse.challa.data.network.dto.BaseResponse
import com.happyhouse.challa.data.network.dto.CreatePhotoRequest
import com.happyhouse.challa.data.network.dto.response.GetPhotoDetailResponse
import com.happyhouse.challa.data.network.dto.response.ListPhotosResponse
import com.happyhouse.challa.domain.result.ChallaResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PhotoApi {
    /** @param page 0부터 시작한다. */
    @GET("api/v1/photos")
    suspend fun getPhotos(
        @Query("roomId") roomId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): ChallaResult<BaseResponse<ListPhotosResponse>>

    /** 사진 본문은 목록 API로 채우고, 이 응답에서는 사진에 달린 반응·댓글만 쓴다. */
    @GET("api/v1/photos/{photoId}")
    suspend fun getPhotoDetail(
        @Path("photoId") photoId: Long,
        @Query("roomId") roomId: Long,
    ): ChallaResult<BaseResponse<GetPhotoDetailResponse>>

    @POST("api/v1/photos")
    suspend fun postPhoto(
        @Body request: CreatePhotoRequest,
    ): ChallaResult<BaseResponse<Unit>>
}
