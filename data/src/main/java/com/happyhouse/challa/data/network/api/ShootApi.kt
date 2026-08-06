package com.happyhouse.challa.data.network.api

import com.happyhouse.challa.data.network.dto.BaseResponse
import com.happyhouse.challa.data.network.dto.response.CameraFilterResponse
import com.happyhouse.challa.domain.result.ChallaResult
import retrofit2.http.GET

interface ShootApi {
    @GET("api/v1/shoots/camera-filters")
    suspend fun getCameraFilters(): ChallaResult<BaseResponse<CameraFilterResponse>>
}
