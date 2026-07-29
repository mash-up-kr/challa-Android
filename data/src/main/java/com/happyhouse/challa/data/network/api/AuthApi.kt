package com.happyhouse.challa.data.network.api

import com.happyhouse.challa.data.network.dto.BaseResponse
import com.happyhouse.challa.data.network.dto.LoginRequest
import com.happyhouse.challa.data.network.dto.LoginResponse
import com.happyhouse.challa.data.network.dto.RefreshRequest
import com.happyhouse.challa.data.network.dto.TokenPairResponse
import com.happyhouse.challa.domain.result.ChallaResult
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest,
    ): ChallaResult<BaseResponse<LoginResponse>>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(
        @Body request: RefreshRequest,
    ): ChallaResult<BaseResponse<TokenPairResponse>>
}
