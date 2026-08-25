package com.happyhouse.challa.presentation.navigation

import com.happyhouse.challa.domain.model.Photo
import java.time.Instant

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
