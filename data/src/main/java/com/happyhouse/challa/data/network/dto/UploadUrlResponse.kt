package com.happyhouse.challa.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class UploadUrlResponse(
    val upload: Upload,
) {
    @Serializable
    data class Upload(
        // S3 업로드용 서명 URL. Authorization 헤더 없이 이미지 바이너리를 PUT 한다. 1회용이며 만료된다.
        val uploadUrl: String,
        // 업로드 성공 후 이미지를 읽을 공개 URL. 만료되지 않으므로 그대로 저장한다.
        val imageUrl: String,
    )
}
