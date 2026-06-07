package com.happyhouse.challa.presentation.sample

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore

/**
 * 비트맵을 degrees 만큼 회전시킨다.
 */
fun Bitmap.rotated(degrees: Int): Bitmap {
    if (degrees == 0) return this
    val m = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(this, 0, 0, width, height, m, true)
}

/**
 * 이미지를 저장한다.
 */
fun saveImage(context: Context, bitmap: Bitmap): Uri? {
    val filename = "Filtered_${System.currentTimeMillis()}.jpg"
    val resolver = context.contentResolver

    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

        // RELATIVE_PATH, IS_PENDING은 Q에서 새로 추가된 필드라 Q 이상에서만 넣음
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/CameraFilterSample"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    var uri: Uri? = null
    return try {
        uri = resolver.insert(collection, values) ?: return null
        resolver.openOutputStream(uri)?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        } ?: throw java.io.IOException("openOutputStream returned null")

        // Q 이상에서 36번째 줄에 IS_PENDING = 1로 "쓰기 중"이라 표시해뒀던 걸, 파일 쓰기가 끝났으니 0으로 되돌려 다른 앱이 볼 수 있게 공개
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
        uri
    } catch (_: Exception) {
        uri?.let { resolver.delete(it, null, null) }
        null
    }
}
