package com.happyhouse.challa.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class UploadUrlRequest(
    val upload: Upload,
) {
    @Serializable
    data class Upload(
        // 이미지의 용도. 저장 위치만 달라진다. (PROFILE_IMAGE, PHOTO)
        val purpose: String,
        // 올릴 이미지의 MIME 타입. 2단계 S3 PUT 의 Content-Type 헤더와 정확히 같아야 한다.
        val contentType: String,
    )
}
