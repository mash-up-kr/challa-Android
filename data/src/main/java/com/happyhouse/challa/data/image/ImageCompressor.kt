package com.happyhouse.challa.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Notice: 아직 압축에 대한 정책이 아무것도 정해진 것이 없기 때문에 임시 코드이며, 리뷰 대상에서 제외한다.
 *
 * 갤러리 등에서 선택한 이미지를 업로드에 적합한 형태로 전처리한다.
 *
 * 원본 해상도를 그대로 올리지 않고 최대 변 기준으로 다운샘플·리사이즈한 뒤 JPEG 로 통일한다.
 * 네트워크와 무관한 순수 이미지 변환 책임만 가지므로 [Context] 외 의존이 없다.
 */
@Singleton
class ImageCompressor
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        /**
         * 이미지를 최대 변 [maxDimension]px 이하로 리사이즈하고 JPEG 로 재압축한 바이트를 돌려준다.
         * 읽을 수 없는 이미지면 null 을 반환한다.
         *
         * - 디코딩 시점에 inSampleSize 로 다운샘플해 원본 풀 비트맵을 힙에 올리지 않는다. (OOM 방지)
         * - 재압축 시 EXIF 가 사라지므로 회전 정보를 픽셀에 미리 반영한다.
         *
         * @param imageUri 갤러리 등에서 선택한 이미지의 content URI 문자열
         */
        suspend fun compressToJpeg(
            imageUri: String,
            maxDimension: Int = DEFAULT_MAX_DIMENSION,
            quality: Int = DEFAULT_JPEG_QUALITY,
        ): ByteArray? =
            withContext(Dispatchers.IO) {
                runCatching {
                    val uri = Uri.parse(imageUri)

                    // 1. 원본 크기만 먼저 읽어 다운샘플 비율을 계산한다.
                    val bounds =
                        BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, bounds)
                    }
                    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null

                    // 2. inSampleSize 로 디코딩 시점부터 메모리 사용량을 줄여 비트맵을 만든다.
                    val decodeOptions =
                        BitmapFactory.Options().apply {
                            inSampleSize =
                                calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
                        }
                    val decoded =
                        context.contentResolver.openInputStream(uri)?.use {
                            BitmapFactory.decodeStream(it, null, decodeOptions)
                        } ?: return@runCatching null

                    // 3. EXIF 회전값을 읽어 정확한 리사이즈 + 회전 비트맵을 만든다.
                    val orientation = readExifOrientation(uri)
                    val normalized =
                        normalizeBitmap(decoded, orientation, maxDimension)

                    // 4. JPEG 로 재압축한다.
                    ByteArrayOutputStream()
                        .use { out ->
                            normalized.compress(Bitmap.CompressFormat.JPEG, quality, out)
                            out.toByteArray()
                        }.also {
                            if (normalized !== decoded) normalized.recycle()
                            decoded.recycle()
                        }
                }.getOrElse { throwable ->
                    // 취소는 실패로 둔갑시키지 않고 그대로 전파한다. (mapCatching 과 동일한 정책)
                    if (throwable is CancellationException) throw throwable
                    null
                }
            }

        private fun readExifOrientation(uri: Uri): Int =
            runCatching {
                context.contentResolver.openInputStream(uri)?.use {
                    ExifInterface(it).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL,
                    )
                }
            }.getOrElse { throwable ->
                if (throwable is CancellationException) throw throwable
                null
            } ?: ExifInterface.ORIENTATION_NORMAL

        /**
         * 비트맵을 최대 변 [maxDimension]px 이하로 축소하고 EXIF 회전을 반영한다.
         * 변경이 필요 없으면 원본 비트맵을 그대로 돌려준다. (호출부에서 동일성 비교로 recycle 판단)
         */
        private fun normalizeBitmap(
            source: Bitmap,
            orientation: Int,
            maxDimension: Int,
        ): Bitmap {
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            }

            val longestSide = maxOf(source.width, source.height)
            if (longestSide > maxDimension) {
                val scale = maxDimension.toFloat() / longestSide
                matrix.postScale(scale, scale)
            }

            if (matrix.isIdentity) return source
            return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }

        private fun calculateInSampleSize(
            width: Int,
            height: Int,
            maxDimension: Int,
        ): Int {
            var sampleSize = 1
            var longestSide = maxOf(width, height)
            while (longestSide / 2 >= maxDimension) {
                longestSide /= 2
                sampleSize *= 2
            }
            return sampleSize
        }

        companion object {
            private const val DEFAULT_MAX_DIMENSION = 1024
            private const val DEFAULT_JPEG_QUALITY = 85
        }
    }
