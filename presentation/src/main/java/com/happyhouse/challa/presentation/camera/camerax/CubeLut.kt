package com.happyhouse.challa.presentation.camera.camerax

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.RawRes

/**
 * `.cube` 3D LUT를 RuntimeShader가 샘플링할 수 있는 2D 비트맵으로 변환합니다.
 *
 * 파란색 축의 각 슬라이스를 가로로 배치해 `width = size²`, `height = size`인 비트맵을 만듭니다.
 * 비트맵 좌표는 `x = blue * size + red`, `y = green`이며, 이 규칙은 프리뷰 LUT 셰이더와
 * 함께 변경되어야 합니다.
 */
internal object CubeLut {
    /**
     * raw 리소스의 `LUT_3D_SIZE`와 RGB 샘플을 읽어 ARGB_8888 비트맵으로 변환합니다.
     * 주석, `TITLE`, `DOMAIN_MIN`, `DOMAIN_MAX` 행은 색상 샘플에서 제외합니다.
     *
     * @throws IllegalArgumentException 크기 선언이 없거나 선언된 크기와 RGB 샘플 수가 다를 때
     */
    fun load(
        resources: Resources,
        @RawRes resourceId: Int,
    ): Bitmap {
        var size: Int? = null
        val colors = mutableListOf<Int>()

        resources.openRawResource(resourceId).bufferedReader().useLines { lines ->
            lines.forEach { rawLine ->
                val line = rawLine.substringBefore('#').trim()
                if (line.isEmpty()) return@forEach

                val values = line.split(WHITESPACE)
                when (values.first()) {
                    "LUT_3D_SIZE" -> size = values.getOrNull(1)?.toIntOrNull()
                    "TITLE", "DOMAIN_MIN", "DOMAIN_MAX" -> Unit
                    else -> {
                        if (values.size >= COLOR_COMPONENT_COUNT) {
                            val red = values[0].toFloatOrNull() ?: return@forEach
                            val green = values[1].toFloatOrNull() ?: return@forEach
                            val blue = values[2].toFloatOrNull() ?: return@forEach
                            colors += Color.rgb(red.toColorByte(), green.toColorByte(), blue.toColorByte())
                        }
                    }
                }
            }
        }

        val lutSize = requireNotNull(size) { "LUT_3D_SIZE가 없는 cube 파일입니다" }
        require(colors.size == lutSize * lutSize * lutSize) {
            "cube 색상 개수가 올바르지 않습니다: expected=${lutSize * lutSize * lutSize}, actual=${colors.size}"
        }

        val pixels = IntArray(colors.size)
        for (blue in 0 until lutSize) {
            for (green in 0 until lutSize) {
                for (red in 0 until lutSize) {
                    val cubeIndex = blue * lutSize * lutSize + green * lutSize + red
                    val bitmapIndex = green * lutSize * lutSize + blue * lutSize + red
                    pixels[bitmapIndex] = colors[cubeIndex]
                }
            }
        }

        return Bitmap.createBitmap(
            pixels,
            lutSize * lutSize,
            lutSize,
            Bitmap.Config.ARGB_8888,
        )
    }
}

private fun Float.toColorByte(): Int = (coerceIn(0f, 1f) * 255f + 0.5f).toInt()

private const val COLOR_COMPONENT_COUNT = 3
private val WHITESPACE = Regex("\\s+")
