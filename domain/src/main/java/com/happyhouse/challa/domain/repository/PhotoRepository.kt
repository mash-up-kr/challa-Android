package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.model.PhotoPage
import com.happyhouse.challa.domain.result.ChallaResult

interface PhotoRepository {
    /** @param page 0부터 시작한다. */
    suspend fun getPhotos(
        roomId: Long,
        page: Int,
    ): ChallaResult<PhotoPage>

    suspend fun savePhoto(imageUrl: String): Result<Unit>
}
