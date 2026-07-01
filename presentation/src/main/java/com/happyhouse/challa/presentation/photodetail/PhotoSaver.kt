package com.happyhouse.challa.presentation.photodetail

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

suspend fun savePhotoToMediaStore(
    context: Context,
    imageUrl: String,
): Result<Unit> =
    withContext(Dispatchers.IO) {
        runCatching {
            val (bytes, format) = loadImageBytes(context, imageUrl)
            writeToMediaStore(context, bytes, format)
        }.onFailure { if (it is CancellationException) throw it }
    }

private suspend fun loadImageBytes(
    context: Context,
    imageUrl: String,
): Pair<ByteArray, ImageFormat> {
    val imageLoader = SingletonImageLoader.get(context)
    val diskCache = imageLoader.diskCache ?: error("Coil 디스크 캐시를 사용할 수 없습니다")

    readFromDiskCache(diskCache, imageUrl)?.let { return it to sniffImageFormat(it) }

    val result = imageLoader.execute(ImageRequest.Builder(context).data(imageUrl).build())
    if (result is ErrorResult) throw result.throwable
    val cacheKey = (result as SuccessResult).diskCacheKey ?: imageUrl
    val bytes = readFromDiskCache(diskCache, cacheKey) ?: error("이미지를 캐시에서 찾을 수 없습니다")
    return bytes to sniffImageFormat(bytes)
}

private fun readFromDiskCache(
    diskCache: DiskCache,
    key: String,
): ByteArray? =
    diskCache.openSnapshot(key)?.use { snapshot ->
        snapshot.data.toFile().readBytes()
    }

private fun sniffImageFormat(bytes: ByteArray): ImageFormat =
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

        else -> ImageFormat.JPEG
    }

private fun writeToMediaStore(
    context: Context,
    bytes: ByteArray,
    format: ImageFormat,
) {
    val resolver = context.contentResolver
    val fileName = "${FILE_NAME_PREFIX}_${System.currentTimeMillis()}.${format.extension}"
    val isScopedStorage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    val values =
        ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, format.mimeType)
            if (isScopedStorage) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/$ALBUM_NAME",
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

    val collection =
        if (isScopedStorage) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

    val uri: Uri = resolver.insert(collection, values) ?: error("MediaStore 레코드 생성 실패")

    try {
        resolver.openOutputStream(uri)?.use { output -> output.write(bytes) }
            ?: error("MediaStore OutputStream 열기 실패")

        if (isScopedStorage) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
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

private const val FILE_NAME_PREFIX = "Challa"
private const val ALBUM_NAME = "Challa"
