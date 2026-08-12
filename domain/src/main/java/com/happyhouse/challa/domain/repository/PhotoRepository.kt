package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.model.Photo
import com.happyhouse.challa.domain.result.ChallaResult

interface PhotoRepository {
    suspend fun getPhotos(roomId: Long): ChallaResult<List<Photo>>

    suspend fun savePhoto(imageUrl: String): Result<Unit>
}
