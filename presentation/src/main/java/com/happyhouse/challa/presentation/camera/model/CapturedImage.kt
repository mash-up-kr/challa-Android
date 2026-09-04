package com.happyhouse.challa.presentation.camera.model

import androidx.compose.runtime.Immutable

/** 촬영 JPEG를 외부 변경 없이 UI 상태에 보관합니다. */
@Immutable
class CapturedImage(
    imageBytes: ByteArray,
) {
    private val bytes = imageBytes.copyOf()

    fun toByteArray(): ByteArray = bytes.copyOf()

    override fun equals(other: Any?): Boolean = this === other || (other is CapturedImage && bytes.contentEquals(other.bytes))

    override fun hashCode(): Int = bytes.contentHashCode()
}
