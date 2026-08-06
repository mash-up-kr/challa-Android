package com.happyhouse.challa.data.network.api

import com.happyhouse.challa.domain.result.ChallaResult
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface CameraFilterFileApi {
    @Streaming
    @GET
    suspend fun getCameraFilterFile(
        @Url fileUrl: String,
    ): ChallaResult<ResponseBody>
}
