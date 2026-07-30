package com.happyhouse.challa.presentation.camera.model

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.happyhouse.challa.presentation.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

/**
 * 카메라 프리뷰에 적용할 수 있는 필터와 버전별 색상 변환 리소스입니다.
 *
 * Android 13 이상에서는 [cubeResId]의 3D LUT를 RuntimeShader로 적용하고,
 * Android 8~12에서는 [fallbackColorMatrix]로 LUT 색감을 선형 근사합니다.
 * [ORIGINAL]은 색상 변환 리소스를 갖지 않습니다.
 *
 * @property labelResId 필터 선택 UI에 표시할 이름 리소스
 * @property cubeResId `.cube` 형식의 raw 리소스. [ORIGINAL]이면 null
 * @property fallbackColorMatrix 4x5 ColorMatrix 값. [ORIGINAL]이면 null
 */
enum class CameraFilter(
    @param:StringRes val labelResId: Int,
    @param:RawRes val cubeResId: Int?,
    val fallbackColorMatrix: FloatArray?,
) {
    ORIGINAL(
        labelResId = R.string.camera_filter_original,
        cubeResId = null,
        fallbackColorMatrix = null,
    ),
    TRUE_CINEMATIC(
        labelResId = R.string.camera_filter_true_cinematic,
        cubeResId = R.raw.true_cinematic,
        fallbackColorMatrix =
            colorMatrix(
                0.752149f,
                0.251299f,
                -0.149222f,
                0.201528f,
                0.091861f,
                0.800557f,
                0.131936f,
                0.028127f,
                0.054965f,
                0.543114f,
                0.545226f,
                0.004821f,
            ),
    ),
    VINTAGE_COLOR(
        labelResId = R.string.camera_filter_vintage_color,
        cubeResId = R.raw.vintage_color,
        fallbackColorMatrix =
            colorMatrix(
                1.157601f,
                -0.091712f,
                -0.107492f,
                0.021178f,
                0.049805f,
                0.94921875f,
                0f,
                0f,
                -0.379444f,
                0.443936f,
                0.847460f,
                0.044024f,
            ),
    ),
    COOL_BLUE_STEEL(
        labelResId = R.string.camera_filter_cool_blue_steel,
        cubeResId = R.raw.cool_blue_steel,
        fallbackColorMatrix =
            colorMatrix(
                0.993160f,
                0f,
                0f,
                -0.049898f,
                0f,
                1.026929f,
                0f,
                -0.050014f,
                0f,
                0f,
                1.110579f,
                -0.004420f,
            ),
    ),
    CLASSIC_BLACK_AND_WHITE(
        labelResId = R.string.camera_filter_classic_black_and_white,
        cubeResId = R.raw.classic_black_and_white,
        fallbackColorMatrix =
            colorMatrix(
                0.221019f,
                0.817159f,
                0.078993f,
                -0.049355f,
                0.231028f,
                0.808112f,
                0.078032f,
                -0.049355f,
                0.230066f,
                0.818121f,
                0.068984f,
                -0.049355f,
            ),
    ),
    CINEMATIC_TEAL_ORANGE(
        labelResId = R.string.camera_filter_cinematic_teal_orange,
        cubeResId = R.raw.cinematic_teal_orange,
        fallbackColorMatrix =
            colorMatrix(
                0.928504f,
                0.192060f,
                0.017400f,
                -0.059852f,
                0.023145f,
                0.956482f,
                0.007855f,
                -0.004497f,
                -0.013371f,
                -0.071508f,
                0.875669f,
                0.110740f,
            ),
    ),
    FADED_KODAK(
        labelResId = R.string.camera_filter_faded_kodak,
        cubeResId = R.raw.faded_kodak,
        fallbackColorMatrix =
            colorMatrix(
                0.890789f,
                0.040142f,
                0.003438f,
                0.054723f,
                -0.008006f,
                0.871698f,
                -0.002527f,
                0.066974f,
                -0.008091f,
                -0.030107f,
                0.837422f,
                0.061458f,
            ),
    ),
    HIGH_CONTRAST(
        labelResId = R.string.camera_filter_high_contrast,
        cubeResId = R.raw.high_contrast,
        fallbackColorMatrix =
            colorMatrix(
                1.150268f,
                0.034813f,
                0.002016f,
                -0.082071f,
                0f,
                1.147727f,
                0f,
                -0.073864f,
                -0.003838f,
                -0.023311f,
                1.144809f,
                -0.066625f,
            ),
    ),
    MATTE_FADE(
        labelResId = R.string.camera_filter_matte_fade,
        cubeResId = R.raw.matte_fade,
        fallbackColorMatrix =
            colorMatrix(
                0.711024f,
                0.135316f,
                0.013660f,
                0.058585f,
                0.040224f,
                0.806116f,
                0.013660f,
                0.050785f,
                0.040224f,
                0.135316f,
                0.684460f,
                0.066385f,
            ),
    ),
    ;

    companion object {
        /** 선택 UI와 인덱스 기반 상태에서 공통으로 사용하는 필터 목록입니다. */
        val availableFilters: ImmutableList<CameraFilter> = entries.toPersistentList()
    }
}

/**
 * 3D LUT를 선형 근사한 RGB 계수와 0~1 범위 오프셋을 Android 4x5 ColorMatrix로 변환합니다.
 * 알파 채널은 변경하지 않으며, RGB 오프셋은 ColorMatrix의 0~255 범위에 맞게 조정합니다.
 */
private fun colorMatrix(
    rr: Float,
    rg: Float,
    rb: Float,
    ro: Float,
    gr: Float,
    gg: Float,
    gb: Float,
    go: Float,
    br: Float,
    bg: Float,
    bb: Float,
    bo: Float,
): FloatArray =
    floatArrayOf(
        rr,
        rg,
        rb,
        0f,
        ro * 255f,
        gr,
        gg,
        gb,
        0f,
        go * 255f,
        br,
        bg,
        bb,
        0f,
        bo * 255f,
        0f,
        0f,
        0f,
        1f,
        0f,
    )
