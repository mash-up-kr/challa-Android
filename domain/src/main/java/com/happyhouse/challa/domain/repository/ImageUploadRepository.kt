package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.result.ChallaResult

interface ImageUploadRepository {
    /**
     * 프로필 이미지를 업로드하고, 저장에 사용할 공개 URL 을 돌려준다.
     *
     * 서버에서 S3 서명 URL 을 발급받아 클라이언트가 직접 S3 로 PUT 하는 방식이며,
     * 성공 시 반환되는 URL 을 그대로 프로필의 `profileImageUrl` 로 저장하면 된다.
     *
     * @param imageUri 갤러리 등에서 선택한 이미지의 content URI 문자열
     */
    suspend fun uploadProfileImage(imageUri: String): ChallaResult<String>

    /** 촬영한 JPEG 이미지를 업로드하고 사진 생성 요청에 사용할 공개 URL을 돌려준다. */
    suspend fun uploadPhoto(imageBytes: ByteArray): ChallaResult<String>
}
