package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.image.ImageCompressor
import com.happyhouse.challa.data.network.api.UploadApi
import com.happyhouse.challa.data.network.dto.UploadUrlRequest
import com.happyhouse.challa.data.network.qualifier.S3UploadClient
import com.happyhouse.challa.domain.repository.ImageUploadRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageUploadRepositoryImpl
    @Inject
    constructor(
        private val imageCompressor: ImageCompressor,
        private val uploadApi: UploadApi,
        @param:S3UploadClient private val s3UploadClient: OkHttpClient,
    ) : ImageUploadRepository {
        override suspend fun uploadProfileImage(imageUri: String): ChallaResult<String> =
            uploadImageFromUri(imageUri = imageUri, purpose = PURPOSE_PROFILE_IMAGE)

        override suspend fun uploadRoomCoverImage(imageUri: String): ChallaResult<String> =
            uploadImageFromUri(imageUri = imageUri, purpose = PURPOSE_ROOM_COVER_IMAGE)

        override suspend fun uploadPhoto(imageBytes: ByteArray): ChallaResult<String> =
            uploadImage(
                purpose = PURPOSE_PHOTO,
                bytes = imageBytes,
            )

        /**
         * 원본 대신 다운샘플·리사이즈 후 JPEG 로 통일한 바이트를 올린다.
         * 따라서 contentType 은 항상 image/jpeg 로 고정된다.
         */
        private suspend fun uploadImageFromUri(
            imageUri: String,
            purpose: String,
        ): ChallaResult<String> {
            val bytes =
                imageCompressor.compressToJpeg(imageUri)
                    ?: return ChallaResult.Failure.Unknown(IllegalStateException("이미지를 읽을 수 없습니다."))

            return uploadImage(purpose = purpose, bytes = bytes)
        }

        private suspend fun uploadImage(
            purpose: String,
            bytes: ByteArray,
        ): ChallaResult<String> =
            // 1단계: 서버에서 S3 업로드용 서명 URL 과 저장에 쓸 공개 URL 을 함께 발급받는다.
            uploadApi
                .postUploadUrl(
                    UploadUrlRequest(
                        upload =
                            UploadUrlRequest.Upload(
                                purpose = purpose,
                                contentType = CONTENT_TYPE_JPEG,
                            ),
                    ),
                ).mapCatching { response ->
                    check(response.success) { response.message }
                    val upload =
                        requireNotNull(response.data?.upload) { "업로드 URL 응답 데이터가 비어 있습니다." }

                    // 2단계: 발급받은 서명 URL 로 이미지 바이너리를 직접 PUT 한다.
                    putImageToS3(
                        uploadUrl = upload.uploadUrl,
                        bytes = bytes,
                        contentType = CONTENT_TYPE_JPEG,
                    )

                    // API 요청에 저장할 공개 URL을 돌려준다.
                    upload.imageUrl
                }

        private suspend fun putImageToS3(
            contentType: String,
            bytes: ByteArray,
            uploadUrl: String,
        ) {
            withContext(Dispatchers.IO) {
                val request =
                    Request
                        .Builder()
                        .url(uploadUrl)
                        // Content-Type 은 1단계 contentType 과 정확히 같아야 하며,
                        // Authorization 헤더는 붙이지 않는다. (S3UploadClient 사용)
                        .put(bytes.toRequestBody(contentType.toMediaType()))
                        .build()

                s3UploadClient.newCall(request).execute().use { response ->
                    check(response.isSuccessful) {
                        "S3 이미지 업로드 실패: HTTP ${response.code}"
                    }
                }
            }
        }

        companion object {
            private const val PURPOSE_PROFILE_IMAGE = "PROFILE_IMAGE"
            private const val PURPOSE_PHOTO = "PHOTO"
            private const val PURPOSE_ROOM_COVER_IMAGE = "ROOM_COVER_IMAGE"
            private const val CONTENT_TYPE_JPEG = "image/jpeg"
        }
    }
