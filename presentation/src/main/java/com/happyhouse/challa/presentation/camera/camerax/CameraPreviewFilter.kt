package com.happyhouse.challa.presentation.camera.camerax

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.happyhouse.challa.presentation.camera.model.CameraFilter

/**
 * 선택한 필터를 PreviewView의 TextureView 출력에 적용합니다.
 *
 * Android 13 이상에서는 준비된 [lutBitmap]을 RuntimeShader로 삼선형 보간합니다.
 * LUT가 준비되지 않았거나 로드에 실패한 경우와 이전 버전에서는 [CameraFilter.fallbackColorMatrix]를 사용하며,
 * [CameraFilter.ORIGINAL]은 적용 중인 모든 색상 효과를 제거합니다.
 *
 * @param filter 새로 적용할 필터
 * @param lutBitmap [CubeLut]가 [filter]의 `.cube` 리소스로 만든 비트맵
 */
internal fun View.applyCameraFilter(
    filter: CameraFilter,
    lutBitmap: Bitmap?,
) {
    clearCameraFilter()

    if (filter == CameraFilter.ORIGINAL) return

    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        lutBitmap != null
    ) {
        setRenderEffect(createLutRenderEffect(lutBitmap))
        return
    }

    val matrix = filter.fallbackColorMatrix ?: return
    val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(ColorMatrix(matrix)) }
    setLayerType(View.LAYER_TYPE_HARDWARE, paint)
}

private fun View.clearCameraFilter() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        setRenderEffect(null)
    }
    setLayerType(View.LAYER_TYPE_NONE, null)
}

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
