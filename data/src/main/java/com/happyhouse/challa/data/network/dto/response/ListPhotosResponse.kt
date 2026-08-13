package com.happyhouse.challa.data.network.dto.response

import com.happyhouse.challa.data.network.parseServerInstant
import com.happyhouse.challa.domain.model.Photo
import com.orhanobut.logger.Logger
import kotlinx.serialization.Serializable

@Serializable
data class ListPhotosResponse(
    val photos: List<PhotoItem>,
    // 기본값을 두면 서버가 필드를 빼먹었을 때 첫 페이지만 받고 조용히 끝난다.
    val hasNext: Boolean,
) {
    @Serializable
    data class PhotoItem(
        val id: Long,
        // 서버는 이미지를 반드시 내려준다고 했지만 스웨거 스펙은 아직 nullable + optional이다.
        // 타입은 약속대로 non-null로 두되, 필드가 빠져도 목록 전체가 MissingFieldException으로
        // 터지지 않도록 기본값을 둔다. 빈 값이 실제로 오면 toPhoto()에서 경고를 남긴다.
        val imageUrl: String = "",
        val userNickname: String? = null,
        val userProfileImageUrl: String = "",
        val createdAt: String,
    )
}

internal fun ListPhotosResponse.PhotoItem.toPhoto(): Photo {
    if (imageUrl.isBlank() || userProfileImageUrl.isBlank()) {
        Logger.t(PHOTO_LOG_TAG).w(
            "사진 응답에 주소가 비어 있습니다. 서버 스펙 확인이 필요합니다. " +
                "photoId=$id, imageUrl=${imageUrl.isNotBlank()}, profileImageUrl=${userProfileImageUrl.isNotBlank()}",
        )
    }

    return Photo(
        id = id,
        imageUrl = imageUrl,
        photographerNickname = userNickname,
        photographerProfileImageUrl = userProfileImageUrl,
        createdAt = createdAt.parseServerInstant(),
    )
}

private const val PHOTO_LOG_TAG = "Photo"
