package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.local.camera.onboarding.CameraOnboardingDataStore
import com.happyhouse.challa.data.network.api.CameraFilterFileApi
import com.happyhouse.challa.data.network.api.PhotoApi
import com.happyhouse.challa.data.network.api.ShootApi
import com.happyhouse.challa.data.network.dto.CreatePhotoRequest
import com.happyhouse.challa.domain.model.CameraFilter
import com.happyhouse.challa.domain.repository.CameraRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import okio.Buffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraRepositoryImpl @Inject constructor(
    private val shootApi: ShootApi,
    private val photoApi: PhotoApi,
    private val cameraFilterFileApi: CameraFilterFileApi,
    private val cameraOnboardingDataStore: CameraOnboardingDataStore,
) : CameraRepository {
    override val hasCompletedOnboarding: Flow<ChallaResult<Boolean>> =
        cameraOnboardingDataStore.hasCompleted

    override suspend fun getCameraFilters(): ChallaResult<List<CameraFilter>> =
        shootApi.getCameraFilters().mapCatching { response ->
            check(response.success) { response.message }
            requireNotNull(response.data) { "카메라 필터 응답 데이터가 비어 있습니다." }
                .shoot
                .cameraFilters
                .map { filter ->
                    CameraFilter(
                        name = filter.name,
                        fileUrl = filter.fileUrl,
                    )
                }
        }

    override suspend fun getCameraFilterFile(fileUrl: String): ChallaResult<ByteArray> =
        withContext(Dispatchers.IO) {
            cameraFilterFileApi.getCameraFilterFile(fileUrl).mapCatching { responseBody ->
                responseBody.readBytesWithLimit(MAX_CAMERA_FILTER_FILE_SIZE_BYTES)
            }
        }

    override suspend fun postPhoto(
        roomId: Long,
        cameraFilterName: String,
        imageUrl: String,
    ): ChallaResult<Unit> =
        photoApi
            .postPhoto(
                CreatePhotoRequest(
                    photo =
                        CreatePhotoRequest.Photo(
                            roomId = roomId,
                            cameraFilterName = cameraFilterName,
                            imageUrl = imageUrl,
                        ),
                ),
            ).mapCatching { response ->
                check(response.success) { response.message }
            }

    override suspend fun completeOnboarding(): ChallaResult<Unit> =
        try {
            cameraOnboardingDataStore.complete()
            ChallaResult.Success(Unit)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            ChallaResult.Failure.Unknown(throwable)
        }
}

private fun ResponseBody.readBytesWithLimit(maxSize: Long): ByteArray =
    use { body ->
        val contentLength = body.contentLength()
        require(contentLength == -1L || contentLength <= maxSize) {
            "카메라 필터 파일이 너무 큽니다: contentLength=$contentLength, maxSize=$maxSize"
        }

        val source = body.source()
        val buffer = Buffer()
        var totalBytes = 0L

        while (true) {
            val bytesToRead = minOf(DEFAULT_READ_SIZE_BYTES, maxSize + 1L - totalBytes)
            val readBytes = source.read(buffer, bytesToRead)
            if (readBytes == -1L) break

            totalBytes += readBytes
            require(totalBytes <= maxSize) {
                "카메라 필터 파일이 너무 큽니다: actualSize>$maxSize"
            }
        }

        buffer.readByteArray()
    }

private const val MAX_CAMERA_FILTER_FILE_SIZE_BYTES = 4L * 1024L * 1024L
private const val DEFAULT_READ_SIZE_BYTES = 8L * 1024L
