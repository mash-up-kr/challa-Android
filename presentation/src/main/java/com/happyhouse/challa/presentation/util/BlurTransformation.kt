package com.happyhouse.challa.presentation.util

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import coil3.size.Size
import coil3.transform.Transformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 박스 블러를 3번 겹쳐 가우시안 블러를 근사하는 Coil 트랜스포메이션.
 *
 * @param radius 축소된 이미지 기준 블러 반경. 값이 클수록 뭉개진다.
 * @param sampleWidth 블러를 계산할 작업 이미지의 가로 크기
 */
class BlurTransformation(
    private val radius: Int = DEFAULT_RADIUS,
    private val sampleWidth: Int = DEFAULT_SAMPLE_WIDTH,
) : Transformation() {
    override val cacheKey: String = "${BlurTransformation::class.java.name}:$radius:$sampleWidth"

    override suspend fun transform(
        input: Bitmap,
        size: Size,
    ): Bitmap =
        withContext(Dispatchers.Default) {
            blur(input)
        }

    private fun blur(input: Bitmap): Bitmap {
        val scaled = input.scaleToSampleWidth()
        val width = scaled.width
        val height = scaled.height

        val source = IntArray(width * height)
        scaled.getPixels(source, 0, width, 0, 0, width, height)
        val destination = IntArray(source.size)

        repeat(BLUR_PASS_COUNT) {
            blurRows(source, destination, width, height, radius)
            blurColumns(destination, source, width, height, radius)
        }

        return createBitmap(width, height).apply {
            setPixels(source, 0, width, 0, 0, width, height)
        }
    }

    private fun Bitmap.scaleToSampleWidth(): Bitmap {
        if (width <= sampleWidth) return this

        val scaledHeight = (height.toLong() * sampleWidth / width).toInt().coerceAtLeast(1)
        return this.scale(sampleWidth, scaledHeight)
    }

    companion object {
        private const val DEFAULT_RADIUS = 12
        private const val DEFAULT_SAMPLE_WIDTH = 96
        private const val BLUR_PASS_COUNT = 3
    }
}

/**
 * 가로 방향 박스 블러
 */
private fun blurRows(
    source: IntArray,
    destination: IntArray,
    width: Int,
    height: Int,
    radius: Int,
) {
    val window = radius * 2 + 1

    for (y in 0 until height) {
        val rowStart = y * width
        var alpha = 0
        var red = 0
        var green = 0
        var blue = 0

        for (offset in -radius..radius) {
            val color = source[rowStart + offset.coerceIn(0, width - 1)]
            alpha += color.alpha()
            red += color.red()
            green += color.green()
            blue += color.blue()
        }

        for (x in 0 until width) {
            destination[rowStart + x] = averageColor(alpha, red, green, blue, window)

            val removed = source[rowStart + (x - radius).coerceIn(0, width - 1)]
            val added = source[rowStart + (x + radius + 1).coerceIn(0, width - 1)]
            alpha += added.alpha() - removed.alpha()
            red += added.red() - removed.red()
            green += added.green() - removed.green()
            blue += added.blue() - removed.blue()
        }
    }
}

/**
 * 세로 방향 박스 블러
 */
private fun blurColumns(
    source: IntArray,
    destination: IntArray,
    width: Int,
    height: Int,
    radius: Int,
) {
    val window = radius * 2 + 1

    for (x in 0 until width) {
        var alpha = 0
        var red = 0
        var green = 0
        var blue = 0

        for (offset in -radius..radius) {
            val color = source[offset.coerceIn(0, height - 1) * width + x]
            alpha += color.alpha()
            red += color.red()
            green += color.green()
            blue += color.blue()
        }

        for (y in 0 until height) {
            destination[y * width + x] = averageColor(alpha, red, green, blue, window)

            val removed = source[(y - radius).coerceIn(0, height - 1) * width + x]
            val added = source[(y + radius + 1).coerceIn(0, height - 1) * width + x]
            alpha += added.alpha() - removed.alpha()
            red += added.red() - removed.red()
            green += added.green() - removed.green()
            blue += added.blue() - removed.blue()
        }
    }
}

private fun averageColor(
    alpha: Int,
    red: Int,
    green: Int,
    blue: Int,
    window: Int,
): Int =
    (alpha / window shl 24) or
        (red / window shl 16) or
        (green / window shl 8) or
        (blue / window)

private fun Int.alpha(): Int = this ushr 24 and 0xFF

private fun Int.red(): Int = this ushr 16 and 0xFF

private fun Int.green(): Int = this ushr 8 and 0xFF

private fun Int.blue(): Int = this and 0xFF
