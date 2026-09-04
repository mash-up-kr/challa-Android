package com.happyhouse.challa.presentation.camera.filter

import android.graphics.Paint
import android.os.Build
import android.view.View
import com.happyhouse.challa.presentation.camera.model.CameraFilterUiModel

/**
 * 선택한 필터를 PreviewView의 TextureView 출력에 적용합니다.
 *
 * Android 13 이상에서는 준비된 [lut]를 RuntimeShader로 삼선형 보간합니다.
 * Android 12 이하에서는 LUT에서 동적으로 만든 ColorMatrix 근삿값을 적용하며,
 * LUT가 준비되지 않았거나 로드에 실패하면 색상 효과를 적용하지 않습니다.
 * [CameraFilterUiModel.Original]은 적용 중인 모든 색상 효과를 제거합니다.
 *
 * @param filter 새로 적용할 필터
 * @param lut [CubeLut]가 [filter]의 원격 `.cube` 파일로 만든 렌더링 데이터
 */
internal fun View.applyCameraFilter(
    filter: CameraFilterUiModel,
    lut: CubeLut.Data?,
) {
    clearCameraFilter()

    if (filter == CameraFilterUiModel.Original) return

    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        lut != null
    ) {
        setRenderEffect(lut.createRenderEffect())
        return
    }

    val fallbackColorFilter = lut?.createColorFilter() ?: return
    val paint = Paint().apply { colorFilter = fallbackColorFilter }
    setLayerType(View.LAYER_TYPE_HARDWARE, paint)
}

private fun View.clearCameraFilter() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        setRenderEffect(null)
    }
    setLayerType(View.LAYER_TYPE_NONE, null)
}
