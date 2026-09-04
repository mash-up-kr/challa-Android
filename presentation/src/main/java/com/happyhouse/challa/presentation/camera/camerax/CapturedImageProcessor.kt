package com.happyhouse.challa.presentation.camera.camerax

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import com.happyhouse.challa.presentation.camera.filter.CubeLut
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * CameraX가 촬영한 JPEG 이미지를 업로드 가능한 바이트 배열로 변환합니다.
 *
 * [process]가 [ImageProxy]의 소유권을 넘겨받아 항상 닫습니다. ImageCapture의 기본 출력인
 * JPEG만 허용하며, 선택한 LUT를 픽셀에 적용한 뒤 회전 방향을 반영해 JPEG로 인코딩합니다.
 * 필터가 없으면 재인코딩 없이 원본 JPEG와 메타데이터를 유지합니다.
 */
internal class CapturedImageProcessor {
    suspend fun process(
        image: ImageProxy,
        lut: CubeLut.Data?,
    ): ByteArray =
        image.use {
            withContext(Dispatchers.Default) {
                require(it.format == ImageFormat.JPEG) { "지원하지 않는 촬영 이미지 포맷입니다: ${it.format}" }
                val buffer =
                    requireNotNull(it.planes.singleOrNull()) { "JPEG 이미지 plane이 유효하지 않습니다." }.buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                require(bytes.isJpeg()) { "촬영 이미지가 올바른 JPEG 데이터가 아닙니다." }
                if (lut == null) return@withContext bytes
                val bitmap =
                    checkNotNull(
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inMutable = true }),
                    ) { "촬영 JPEG를 디코딩하지 못했습니다." }
                try {
                    lut.applyTo(bitmap)
                    val rotated =
                        Bitmap.createBitmap(
                            bitmap,
                            0,
                            0,
                            bitmap.width,
                            bitmap.height,
                            Matrix().apply { postRotate(it.imageInfo.rotationDegrees.toFloat()) },
                            true,
                        )
                    try {
                        ByteArrayOutputStream().use { output ->
                            check(rotated.compress(Bitmap.CompressFormat.JPEG, 95, output)) { "촬영 JPEG를 인코딩하지 못했습니다." }
                            output.toByteArray()
                        }
                    } finally {
                        if (rotated !== bitmap) rotated.recycle()
                    }
                } finally {
                    bitmap.recycle()
                }
            }
        }
}

private fun ByteArray.isJpeg(): Boolean =
    size >= 3 &&
        this[0] == 0xFF.toByte() &&
        this[1] == 0xD8.toByte() &&
        this[2] == 0xFF.toByte()
