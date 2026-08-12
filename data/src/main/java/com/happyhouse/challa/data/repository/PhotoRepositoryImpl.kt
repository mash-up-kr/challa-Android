package com.happyhouse.challa.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.happyhouse.challa.data.network.api.PhotoApi
import com.happyhouse.challa.data.network.dto.response.ListPhotosResponse
import com.happyhouse.challa.data.network.dto.response.toPhoto
import com.happyhouse.challa.domain.model.Photo
import com.happyhouse.challa.domain.repository.PhotoRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class PhotoRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val photoApi: PhotoApi,
) : PhotoRepository {
    /**
     * 방의 사진을 첫 페이지부터 [hasNext][ListPhotosResponse.hasNext] 가 없을 때까지 이어 받아 한 번에 돌려준다.
     *
     * 갤러리·사진 상세 모두 방의 사진 전부가 필요해서 페이지를 호출부로 노출하지 않는다.
     */
    override suspend fun getPhotos(roomId: Long): ChallaResult<List<Photo>> {
        val photos = mutableListOf<Photo>()

        for (page in 0 until MAX_PHOTO_PAGE_COUNT) {
            val pageResult =
                photoApi
                    .getPhotos(roomId = roomId, page = page, size = PHOTO_PAGE_SIZE)
                    .mapCatching { response ->
                        check(response.success) { response.message }
                        requireNotNull(response.data) { "사진 목록 응답 데이터가 비어 있습니다." }
                    }

            val pageData =
                when (pageResult) {
                    is ChallaResult.Success -> pageResult.data
                    is ChallaResult.Failure -> return pageResult
                }

            val mapped =
                runCatching { pageData.photos.map { it.toPhoto() } }
                    .getOrElse { throwable ->
                        if (throwable is CancellationException) throw throwable
                        return ChallaResult.Failure.Unknown(throwable)
                    }

            photos += mapped
            // 페이지를 받는 사이에 사진이 늘면 같은 사진이 두 페이지에 걸쳐 오고, 목록 화면의 key가 겹쳐 깨진다.
            if (!pageData.hasNext) return ChallaResult.Success(photos.distinctBy { it.id })
        }

        // 상한까지 hasNext가 남아 있으면 서버가 끝을 알려주지 않는 것이라, 무한히 받지 않고 실패로 끊는다.
        return ChallaResult.Failure.Unknown(
            IllegalStateException("사진 목록 페이지가 ${MAX_PHOTO_PAGE_COUNT}개를 넘었습니다. roomId=$roomId"),
        )
    }

    override suspend fun savePhoto(imageUrl: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val (bytes, format) = loadImageBytes(imageUrl)
                writeToMediaStore(bytes, format)
            }.onFailure { if (it is CancellationException) throw it }
        }

    private suspend fun loadImageBytes(imageUrl: String): Pair<ByteArray, ImageFormat> {
        val imageLoader = SingletonImageLoader.get(context)
        val diskCache = imageLoader.diskCache ?: error("Coil 디스크 캐시를 사용할 수 없습니다")

        val bytes =
            readFromDiskCache(diskCache, imageUrl) ?: run {
                val result = imageLoader.execute(ImageRequest.Builder(context).data(imageUrl).build())
                if (result is ErrorResult) throw result.throwable
                val cacheKey = (result as SuccessResult).diskCacheKey ?: imageUrl
                readFromDiskCache(diskCache, cacheKey) ?: error("이미지를 캐시에서 찾을 수 없습니다")
            }

        val format = sniffImageFormat(bytes) ?: error("지원하지 않는 이미지 포맷입니다")
        return bytes to format
    }

    private fun readFromDiskCache(
        diskCache: DiskCache,
        key: String,
    ): ByteArray? =
        diskCache.openSnapshot(key)?.use { snapshot ->
            snapshot.data.toFile().readBytes()
        }

    private fun sniffImageFormat(bytes: ByteArray): ImageFormat? =
        when {
            bytes.size >= 3 &&
                bytes[0] == 0xFF.toByte() &&
                bytes[1] == 0xD8.toByte() &&
                bytes[2] == 0xFF.toByte() -> ImageFormat.JPEG

            bytes.size >= 8 &&
                bytes[0] == 0x89.toByte() &&
                bytes[1] == 0x50.toByte() &&
                bytes[2] == 0x4E.toByte() &&
                bytes[3] == 0x47.toByte() -> ImageFormat.PNG

            bytes.size >= 12 &&
                bytes[0] == 'R'.code.toByte() &&
                bytes[1] == 'I'.code.toByte() &&
                bytes[2] == 'F'.code.toByte() &&
                bytes[3] == 'F'.code.toByte() &&
                bytes[8] == 'W'.code.toByte() &&
                bytes[9] == 'E'.code.toByte() &&
                bytes[10] == 'B'.code.toByte() &&
                bytes[11] == 'P'.code.toByte() -> ImageFormat.WEBP

            else -> null
        }

    private fun writeToMediaStore(
        bytes: ByteArray,
        format: ImageFormat,
    ) {
        val resolver = context.contentResolver
        val fileName = "${FILE_NAME_PREFIX}_${System.currentTimeMillis()}.${format.extension}"
        val isScopedStorage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        val values =
            ContentValues().apply {
                put(Media.DISPLAY_NAME, fileName)
                put(Media.MIME_TYPE, format.mimeType)
                if (isScopedStorage) {
                    put(
                        Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/$ALBUM_NAME",
                    )
                    put(Media.IS_PENDING, 1)
                }
            }

        val collection =
            if (isScopedStorage) {
                Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                Media.EXTERNAL_CONTENT_URI
            }

        val uri: Uri = resolver.insert(collection, values) ?: error("MediaStore 레코드 생성 실패")

        try {
            resolver.openOutputStream(uri)?.use { output -> output.write(bytes) }
                ?: error("MediaStore OutputStream 열기 실패")

            if (isScopedStorage) {
                values.clear()
                values.put(Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
        } catch (throwable: Throwable) {
            resolver.delete(uri, null, null)
            throw throwable
        }
    }

    private enum class ImageFormat(
        val mimeType: String,
        val extension: String,
    ) {
        JPEG("image/jpeg", "jpg"),
        PNG("image/png", "png"),
        WEBP("image/webp", "webp"),
    }

    companion object {
        private const val FILE_NAME_PREFIX = "Challa"
        private const val ALBUM_NAME = "Challa"

        /** 사진 목록 한 페이지 크기. 방 최대 촬영 수에 맞춰, 정상적인 방은 요청 한 번으로 끝나게 한다. */
        private const val PHOTO_PAGE_SIZE = 72

        /** 이어 받을 페이지 상한 */
        private const val MAX_PHOTO_PAGE_COUNT = 10
    }
}
