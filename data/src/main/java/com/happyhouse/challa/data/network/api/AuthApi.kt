package com.happyhouse.challa.data.network.api

import com.happyhouse.challa.data.network.dto.BaseResponse
import com.happyhouse.challa.data.network.dto.LogoutRequest
import com.happyhouse.challa.data.network.dto.request.LoginRequest
import com.happyhouse.challa.data.network.dto.request.RefreshRequest
import com.happyhouse.challa.data.network.dto.response.LoginResponse
import com.happyhouse.challa.data.network.dto.response.TokenPairResponse
import com.happyhouse.challa.domain.result.ChallaResult
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest,
    ): ChallaResult<BaseResponse<LoginResponse>>

    @POST("api/v1/auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest,
    ): ChallaResult<BaseResponse<Unit>>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(
        @Body request: RefreshRequest,
    ): ChallaResult<BaseResponse<TokenPairResponse>>
}
