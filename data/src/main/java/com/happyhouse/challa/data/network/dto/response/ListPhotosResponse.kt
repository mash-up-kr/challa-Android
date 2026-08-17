package com.happyhouse.challa.data.network.dto.response

import com.happyhouse.challa.data.network.parseServerInstant
import com.happyhouse.challa.domain.model.Photo
import com.orhanobut.logger.Logger
import kotlinx.serialization.Serializable

@Serializable
data class ListPhotosResponse(
    val photos: List<PhotoItem>,
    val hasNext: Boolean,
) {
    @Serializable
    data class PhotoItem(
        val id: Long,
        // 스웨거상 필수 필드가 아니라 응답에서 빠질 수 있다. 타입은 non-null로 두고 파싱만 방어한다.
        val imageUrl: String = "",
        val userNickname: String,
        val userProfileImageUrl: String? = null,
        val createdAt: String,
    )
}

internal fun ListPhotosResponse.PhotoItem.toPhoto(): Photo {
    if (imageUrl.isBlank() || userNickname.isBlank()) {
        Logger.t(PHOTO_LOG_TAG).w(
            "사진 응답에 필수 값이 비어 있습니다. 서버 스펙 확인이 필요합니다. " +
                "photoId=$id, hasImageUrl=${imageUrl.isNotBlank()}, hasNickname=${userNickname.isNotBlank()}",
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
