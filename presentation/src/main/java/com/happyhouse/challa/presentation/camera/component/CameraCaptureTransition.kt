package com.happyhouse.challa.presentation.camera.component

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asComposeColorFilter
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.happyhouse.challa.presentation.camera.filter.CubeLut
import com.happyhouse.challa.presentation.camera.model.CapturedImage

@Composable
internal fun CameraCaptureTransition(
    image: CapturedImage,
    lut: CubeLut.Data?,
    isFilterReady: Boolean,
    onAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageBytes = remember(image) { image.toByteArray() }
    val progress = remember(image) { Animatable(0f) }
    var isImageReady by remember(image) { mutableStateOf(false) }
    val currentOnAnimationFinished by rememberUpdatedState(onAnimationFinished)
    val filterEffect =
        remember(lut) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && lut != null) {
                lut.createRenderEffect().asComposeRenderEffect()
            } else {
                null
            }
        }
    val fallbackColorFilter =
        remember(lut) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && lut != null) {
                lut.createColorFilter().asComposeColorFilter()
            } else {
                null
            }
        }

    LaunchedEffect(image, isImageReady, isFilterReady) {
        if (!isImageReady || !isFilterReady) return@LaunchedEffect
        progress.animateTo(1f, tween(durationMillis = 500, easing = FastOutSlowInEasing))
        currentOnAnimationFinished()
    }

    BoxWithConstraints(
        modifier =
            modifier.pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach { it.consume() }
                    }
                }
            },
    ) {
        val bezelWidth = maxWidth - CameraBezelHorizontalPadding * 2
        val bezelHeight = bezelWidth / CAMERA_BEZEL_ASPECT_RATIO
        val centeredTop = (maxHeight - bezelHeight) / 2
        val top = CameraBezelTopPadding + (centeredTop - CameraBezelTopPadding) * progress.value

        CameraBezel(
            modifier =
                Modifier
                    .offset(x = CameraBezelHorizontalPadding, y = top)
                    .size(width = bezelWidth, height = bezelHeight),
            isPhotoLimitReached = false,
            isShutterEffectVisible = false,
            zoomLevel = 1f,
            onZoomClick = {},
            showZoom = false,
        ) { imageModifier ->
            AsyncImage(
                model = imageBytes,
                contentDescription = null,
                modifier =
                    imageModifier
                        .alpha(if (isFilterReady) 1f else 0f)
                        .graphicsLayer { renderEffect = filterEffect },
                colorFilter = fallbackColorFilter,
                contentScale = ContentScale.Crop,
                onSuccess = { isImageReady = true },
                // 디코딩 실패가 저장 완료 후 화면 이동을 막지 않도록 한다.
                onError = { isImageReady = true },
            )
        }
    }
}
