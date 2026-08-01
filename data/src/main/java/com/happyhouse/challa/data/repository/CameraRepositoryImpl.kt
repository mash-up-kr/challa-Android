package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.network.api.CameraApi
import com.happyhouse.challa.domain.model.CameraFilter
import com.happyhouse.challa.domain.repository.CameraRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraRepositoryImpl @Inject constructor(
    private val cameraApi: CameraApi,
) : CameraRepository {
    override suspend fun getCameraFilters(): ChallaResult<List<CameraFilter>> =
        cameraApi.getCameraFilters().mapCatching { response ->
            check(response.success) { response.message }
            requireNotNull(response.data) { "카메라 필터 응답 데이터가 비어 있습니다." }
                .cameraFilters
                .map { filter ->
                    CameraFilter(
                        id = filter.id,
                        name = filter.name,
                        fileUrl = filter.fileUrl,
                    )
                }
        }
}
