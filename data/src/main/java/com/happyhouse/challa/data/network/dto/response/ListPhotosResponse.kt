package com.happyhouse.challa.data.network.dto.response

import com.happyhouse.challa.data.network.parseServerInstant
import com.happyhouse.challa.domain.model.Photo
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
        val imageUrl: String,
        val userNickname: String? = null,
        val userProfileImageUrl: String,
        val createdAt: String,
    )
}

internal fun ListPhotosResponse.PhotoItem.toPhoto(): Photo =
    Photo(
        id = id,
        imageUrl = imageUrl,
        photographerNickname = userNickname,
        photographerProfileImageUrl = userProfileImageUrl,
        createdAt = createdAt.parseServerInstant(),
    )
