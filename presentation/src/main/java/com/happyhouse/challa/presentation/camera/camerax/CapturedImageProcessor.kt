package com.happyhouse.challa.presentation.camera.camerax

import android.graphics.ImageFormat
import androidx.camera.core.ImageProxy

/**
 * CameraX가 촬영한 JPEG 이미지를 업로드 가능한 바이트 배열로 변환합니다.
 *
 * [process]가 [ImageProxy]의 소유권을 넘겨받아 항상 닫습니다. ImageCapture의 기본 출력인
 * JPEG만 허용하며, 촬영 필터는 서버가 `cameraFilterName`을 기준으로 처리합니다.
 */
internal class CapturedImageProcessor {
    fun process(image: ImageProxy): ByteArray =
        image.use {
            require(it.format == ImageFormat.JPEG) { "지원하지 않는 촬영 이미지 포맷입니다: ${it.format}" }
            val buffer =
                requireNotNull(it.planes.singleOrNull()) { "JPEG 이미지 plane이 유효하지 않습니다." }.buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            require(bytes.isJpeg()) { "촬영 이미지가 올바른 JPEG 데이터가 아닙니다." }
            bytes
        }
}

private fun ByteArray.isJpeg(): Boolean =
    size >= 3 &&
        this[0] == 0xFF.toByte() &&
        this[1] == 0xD8.toByte() &&
        this[2] == 0xFF.toByte()
