package com.happyhouse.challa.data.network.api

import com.happyhouse.challa.data.network.dto.BaseResponse
import com.happyhouse.challa.data.network.dto.request.UpdateProfileRequest
import com.happyhouse.challa.data.network.dto.response.UserProfileResponse
import com.happyhouse.challa.domain.result.ChallaResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApi {
    @GET("api/v1/users/me")
    suspend fun getMyProfile(): ChallaResult<BaseResponse<UserProfileResponse>>

    @PUT("api/v1/users/me")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest,
    ): ChallaResult<BaseResponse<UserProfileResponse>>
}
