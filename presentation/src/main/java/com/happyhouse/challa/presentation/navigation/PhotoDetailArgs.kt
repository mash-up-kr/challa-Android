package com.happyhouse.challa.presentation.navigation

import com.happyhouse.challa.domain.model.Photo
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * 갤러리가 이미 받아둔 사진과 페이징 위치.
 *
 * 상세가 같은 목록을 다시 조회하지 않도록 그대로 넘기고, 끝까지 넘겨 갤러리가 받아둔 범위를 벗어나면
 * [nextPhotoPage] 부터 이어 받는다.
 */
@Serializable
data class PhotoDetailArgs(
    val roomName: String,
    val photos: List<PhotoArg>,
    val nextPhotoPage: Int,
    val hasNextPhotoPage: Boolean,
) {
    @Serializable
    data class PhotoArg(
        val id: Long,
        val imageUrl: String,
        val photographerNickname: String,
        val photographerProfileImageUrl: String?,
        val createdAtEpochMillis: Long,
    )
}

internal fun List<Photo>.toPhotoArgs(): List<PhotoDetailArgs.PhotoArg> =
    map { photo ->
        PhotoDetailArgs.PhotoArg(
            id = photo.id,
            imageUrl = photo.imageUrl,
            photographerNickname = photo.photographerNickname,
            photographerProfileImageUrl = photo.photographerProfileImageUrl,
            createdAtEpochMillis = photo.createdAt.toEpochMilli(),
        )
    }

internal fun List<PhotoDetailArgs.PhotoArg>.toPhotos(): List<Photo> =
    map { arg ->
        Photo(
            id = arg.id,
            imageUrl = arg.imageUrl,
            photographerNickname = arg.photographerNickname,
            photographerProfileImageUrl = arg.photographerProfileImageUrl,
            createdAt = Instant.ofEpochMilli(arg.createdAtEpochMillis),
        )
    }
