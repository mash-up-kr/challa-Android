package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.model.CameraFilter
import com.happyhouse.challa.domain.result.ChallaResult

interface CameraRepository {
    suspend fun getCameraFilters(): ChallaResult<List<CameraFilter>>

    suspend fun getCameraFilterFile(fileUrl: String): ChallaResult<ByteArray>

    suspend fun postPhoto(
        roomId: Long,
        cameraFilterName: String,
        imageUrl: String,
    ): ChallaResult<Unit>
}
