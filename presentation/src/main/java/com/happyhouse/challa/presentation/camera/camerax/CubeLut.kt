package com.happyhouse.challa.presentation.camera.camerax

import android.graphics.Bitmap
import android.graphics.Color

/**
 * `.cube` 3D LUT를 RuntimeShader가 샘플링할 수 있는 2D 비트맵으로 변환합니다.
 *
 * 파란색 축의 각 슬라이스를 가로로 배치해 `width = size²`, `height = size`인 비트맵을 만듭니다.
 * 비트맵 좌표는 `x = blue * size + red`, `y = green`이며, 이 규칙은 프리뷰 LUT 셰이더와
 * 함께 변경되어야 합니다.
 */
internal object CubeLut {
    class Data(
        val bitmap: Bitmap,
        val fallbackColorMatrix: FloatArray,
    )

    /**
     * `.cube` 파일 바이트의 `LUT_3D_SIZE`와 RGB 샘플을 읽어 ARGB_8888 비트맵으로 변환합니다.
     * 주석, `TITLE`, `DOMAIN_MIN`, `DOMAIN_MAX` 행은 색상 샘플에서 제외합니다.
     *
     * @throws IllegalArgumentException 크기 선언이 없거나 선언된 크기와 RGB 샘플 수가 다를 때
     */
    fun load(cubeFile: ByteArray): Data {
        var size: Int? = null
        val colors = mutableListOf<Int>()

        cubeFile.inputStream().bufferedReader().useLines { lines ->
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
                            colors +=
                                Color.rgb(
                                    red.toColorByte(),
                                    green.toColorByte(),
                                    blue.toColorByte(),
                                )
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

        return Data(
            bitmap =
                Bitmap.createBitmap(
                    pixels,
                    lutSize * lutSize,
                    lutSize,
                    Bitmap.Config.ARGB_8888,
                ),
            fallbackColorMatrix = createFallbackColorMatrix(colors, lutSize),
        )
    }

    /** 3D LUT의 검정·RGB 기준점을 Android 4x5 ColorMatrix로 선형 근사합니다. */
    private fun createFallbackColorMatrix(
        colors: List<Int>,
        lutSize: Int,
    ): FloatArray {
        val black = colors.first()
        val red = colors[lutSize - 1]
        val green = colors[(lutSize - 1) * lutSize]
        val blue = colors[(lutSize - 1) * lutSize * lutSize]

        fun coefficient(
            color: Int,
            base: Int,
            component: (Int) -> Int,
        ): Float = (component(color) - component(base)) / COLOR_BYTE_MAX

        return floatArrayOf(
            coefficient(red, black, Color::red),
            coefficient(green, black, Color::red),
            coefficient(blue, black, Color::red),
            0f,
            Color.red(black).toFloat(),
            coefficient(red, black, Color::green),
            coefficient(green, black, Color::green),
            coefficient(blue, black, Color::green),
            0f,
            Color.green(black).toFloat(),
            coefficient(red, black, Color::blue),
            coefficient(green, black, Color::blue),
            coefficient(blue, black, Color::blue),
            0f,
            Color.blue(black).toFloat(),
            0f,
            0f,
            0f,
            1f,
            0f,
        )
    }
}

private fun Float.toColorByte(): Int = (coerceIn(0f, 1f) * 255f + 0.5f).toInt()

private const val COLOR_COMPONENT_COUNT = 3
private const val COLOR_BYTE_MAX = 255f
private val WHITESPACE = Regex("\\s+")
