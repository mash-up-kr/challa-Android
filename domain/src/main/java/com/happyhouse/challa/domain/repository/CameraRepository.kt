package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.model.CameraFilter
import com.happyhouse.challa.domain.result.ChallaResult

interface CameraRepository {
    suspend fun getCameraFilters(): ChallaResult<List<CameraFilter>>
}
