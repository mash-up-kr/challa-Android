package com.happyhouse.challa.domain.repository

interface PhotoRepository {
    suspend fun savePhoto(imageUrl: String): Result<Unit>
}
