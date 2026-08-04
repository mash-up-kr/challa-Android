package com.happyhouse.challa.data.network.api

import com.happyhouse.challa.data.network.dto.BaseResponse
import com.happyhouse.challa.data.network.dto.UploadUrlRequest
import com.happyhouse.challa.data.network.dto.UploadUrlResponse
import com.happyhouse.challa.domain.result.ChallaResult
import retrofit2.http.Body
import retrofit2.http.POST

interface UploadApi {
    /**
     * 이미지 업로드용 S3 서명 URL 을 발급받는다. 실제 파일은 서버로 보내지 않고,
     * 응답으로 받은 `uploadUrl` 로 클라이언트가 직접 S3 에 PUT 한다.
     */
    @POST("api/v1/uploads")
    suspend fun postUploadUrl(
        @Body request: UploadUrlRequest,
    ): ChallaResult<BaseResponse<UploadUrlResponse>>
}
