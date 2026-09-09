package com.happyhouse.challa.presentation.camera.filter

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * `.cube` 3D LUT를 RuntimeShader가 샘플링할 수 있는 2D 비트맵으로 변환합니다.
 *
 * 파란색 축의 각 슬라이스를 가로로 배치해 `width = size²`, `height = size`인 비트맵을 만듭니다.
 * 비트맵 좌표는 `x = blue * size + red`, `y = green`이며, 이 규칙은 프리뷰 LUT 셰이더와
 * 함께 변경되어야 합니다.
 */
internal object CubeLut {
    @Immutable
    class Data(
        pixels: IntArray,
        size: Int,
        fallbackColorMatrix: FloatArray,
    ) {
        private val bitmap = Bitmap.createBitmap(pixels, size * size, size, Bitmap.Config.ARGB_8888)
        private val colorMatrix = fallbackColorMatrix.copyOf()

        /**
         * 촬영 비트맵에 프리뷰와 같은 필터 계산 방식을 적용합니다.
         * Android 13 이상은 삼선형 LUT 보간, Android 12 이하는 ColorMatrix 근사를 사용합니다.
         */
        suspend fun applyTo(image: Bitmap) {
            val usesColorMatrix = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            val size = bitmap.height
            val samples = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(samples, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            val row = IntArray(image.width)
            for (y in 0 until image.height) {
                currentCoroutineContext().ensureActive()
                image.getPixels(row, 0, image.width, 0, y, image.width, 1)
                for (x in row.indices) {
                    val color = row[x]
                    if (usesColorMatrix) {
                        row[x] = applyColorMatrix(color)
                        continue
                    }
                    val red = Color.red(color) * (size - 1) / 255f
                    val green = Color.green(color) * (size - 1) / 255f
                    val blue = Color.blue(color) * (size - 1) / 255f
                    val r0 = red.toInt()
                    val g0 = green.toInt()
                    val b0 = blue.toInt()
                    val r1 = (r0 + 1).coerceAtMost(size - 1)
                    val g1 = (g0 + 1).coerceAtMost(size - 1)
                    val b1 = (b0 + 1).coerceAtMost(size - 1)

                    fun sample(
                        r: Int,
                        g: Int,
                        b: Int,
                    ): Int = samples[g * size * size + b * size + r]
                    val c000 = sample(r0, g0, b0)
                    val c100 = sample(r1, g0, b0)
                    val c010 = sample(r0, g1, b0)
                    val c110 = sample(r1, g1, b0)
                    val c001 = sample(r0, g0, b1)
                    val c101 = sample(r1, g0, b1)
                    val c011 = sample(r0, g1, b1)
                    val c111 = sample(r1, g1, b1)

                    fun channel(shift: Int): Int {
                        fun component(value: Int): Float = ((value shr shift) and 255).toFloat()

                        fun mix(
                            a: Float,
                            b: Float,
                            fraction: Float,
                        ): Float = a + (b - a) * fraction
                        val low =
                            mix(
                                mix(component(c000), component(c100), red - r0),
                                mix(component(c010), component(c110), red - r0),
                                green - g0,
                            )
                        val high =
                            mix(
                                mix(component(c001), component(c101), red - r0),
                                mix(component(c011), component(c111), red - r0),
                                green - g0,
                            )
                        return (mix(low, high, blue - b0) + 0.5f).toInt().coerceIn(0, 255)
                    }
                    row[x] = Color.rgb(channel(16), channel(8), channel(0))
                }
                image.setPixels(row, 0, image.width, 0, y, image.width, 1)
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun createRenderEffect(): RenderEffect = createLutRenderEffect(bitmap)

        fun createColorFilter(): ColorMatrixColorFilter = ColorMatrixColorFilter(colorMatrix)

        private fun applyColorMatrix(color: Int): Int {
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)
            val alpha = Color.alpha(color)

            fun channel(offset: Int): Int {
                val value =
                    colorMatrix[offset] * red +
                        colorMatrix[offset + 1] * green +
                        colorMatrix[offset + 2] * blue +
                        colorMatrix[offset + 3] * alpha +
                        colorMatrix[offset + 4]
                return (value.coerceIn(0f, 255f) + 0.5f).toInt()
            }

            return Color.argb(channel(15), channel(0), channel(5), channel(10))
        }
    }

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
            pixels = pixels,
            size = lutSize,
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

/** LUT 비트맵의 인접한 8개 RGB 샘플을 보간하는 RuntimeShader 효과를 만듭니다. */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun createLutRenderEffect(lutBitmap: Bitmap): RenderEffect {
    val lutSize = lutBitmap.height.toFloat()
    val runtimeShader = RuntimeShader(LUT_SHADER)
    val bitmapShader =
        BitmapShader(lutBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
            filterMode = BitmapShader.FILTER_MODE_NEAREST
        }
    runtimeShader.setInputShader("lut", bitmapShader)
    runtimeShader.setFloatUniform("lutSize", lutSize)
    return RenderEffect.createRuntimeShaderEffect(runtimeShader, "content")
}

private const val LUT_SHADER = """
    uniform shader content;
    uniform shader lut;
    uniform float lutSize;

    half3 lookup(float3 color) {
        float3 position = clamp(color, 0.0, 1.0) * (lutSize - 1.0);
        float3 lower = floor(position);
        float3 upper = min(lower + 1.0, lutSize - 1.0);
        float3 fraction = position - lower;

        half3 c000 = lut.eval(float2(lower.x + lower.z * lutSize + 0.5, lower.y + 0.5)).rgb;
        half3 c100 = lut.eval(float2(upper.x + lower.z * lutSize + 0.5, lower.y + 0.5)).rgb;
        half3 c010 = lut.eval(float2(lower.x + lower.z * lutSize + 0.5, upper.y + 0.5)).rgb;
        half3 c110 = lut.eval(float2(upper.x + lower.z * lutSize + 0.5, upper.y + 0.5)).rgb;
        half3 c001 = lut.eval(float2(lower.x + upper.z * lutSize + 0.5, lower.y + 0.5)).rgb;
        half3 c101 = lut.eval(float2(upper.x + upper.z * lutSize + 0.5, lower.y + 0.5)).rgb;
        half3 c011 = lut.eval(float2(lower.x + upper.z * lutSize + 0.5, upper.y + 0.5)).rgb;
        half3 c111 = lut.eval(float2(upper.x + upper.z * lutSize + 0.5, upper.y + 0.5)).rgb;

        half3 lowBlue = mix(mix(c000, c100, fraction.x), mix(c010, c110, fraction.x), fraction.y);
        half3 highBlue = mix(mix(c001, c101, fraction.x), mix(c011, c111, fraction.x), fraction.y);
        return mix(lowBlue, highBlue, fraction.z);
    }

    half4 main(float2 coordinate) {
        half4 source = content.eval(coordinate);
        return half4(lookup(source.rgb), source.a);
    }
"""
