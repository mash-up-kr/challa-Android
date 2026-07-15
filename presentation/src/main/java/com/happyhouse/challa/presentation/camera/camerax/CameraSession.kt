package com.happyhouse.challa.presentation.camera.camerax

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.takePicture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.happyhouse.challa.presentation.camera.contract.CameraLensFacing
import com.happyhouse.challa.presentation.camera.model.PhotoCaptureRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import timber.log.Timber
import androidx.camera.core.Camera as CameraXCamera

/**
 * CameraX 세션에서 UI가 알아야 하는 최소 상태입니다.
 *
 * @property isReady Preview와 [ImageCapture]가 바인딩되어 촬영할 수 있는지 여부
 * @property isCapturing 사진 한 장의 촬영 요청을 처리하고 있는지 여부
 */
@Immutable
internal data class CameraSessionState(
    val isReady: Boolean = false,
    val isCapturing: Boolean = false,
)

private data class BoundCameraUseCases(
    val camera: CameraXCamera,
    val preview: Preview,
    val imageCapture: ImageCapture,
)

/**
 * CameraX Preview와 [ImageCapture]의 생성, 바인딩, 제어를 담당합니다.
 *
 * [lensFacing]이 변경되면 기존 UseCase를 해제하고 새 카메라에 다시 바인딩합니다.
 * 촬영 플래시 모드를 적용하고 줌 제어가 완료될 때까지 비동기로 기다리며, 실패한 제어 요청은 기록합니다.
 * 새로운 [captureRequest]가 전달되면 이미지를 메모리로 촬영하고 즉시 닫은 뒤 성공 여부만 반환합니다.
 * Composable이 Composition에서 제거되면 진행 중인 초기화와 촬영 코루틴도 취소됩니다.
 *
 * @param captureRequest 현재 세션에서 처리할 촬영 요청. [PhotoCaptureRequest.requestId]가 바뀔 때마다
 * 새 요청으로 처리하며, 세션 재진입 시 재처리를 막기 위해 호출자는 결과를 받은 뒤 요청을 제거해야 합니다.
 */
@Composable
internal fun CameraSession(
    modifier: Modifier = Modifier,
    lensFacing: CameraLensFacing,
    isFlashEnabled: Boolean,
    zoomLevel: Int,
    captureRequest: PhotoCaptureRequest?,
    onEvent: (CameraSessionEvent) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember(context) { createPreviewView(context) }
    val currentOnEvent by rememberUpdatedState(onEvent)
    var camera by remember { mutableStateOf<CameraXCamera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var sessionState by remember { mutableStateOf(CameraSessionState()) }

    // Composable 및 렌즈의 생명주기에 맞춰 CameraX UseCase를 바인딩하고 해제합니다.
    LaunchedEffect(context, lifecycleOwner, previewView, lensFacing) {
        try {
            val provider = ProcessCameraProvider.awaitInstance(context)

            val boundUseCases =
                bindCameraUseCases(
                    cameraProvider = provider,
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView,
                    lensFacing = lensFacing,
                )

            try {
                camera = boundUseCases.camera
                imageCapture = boundUseCases.imageCapture
                sessionState = sessionState.copy(isReady = true)
                currentOnEvent(CameraSessionEvent.StateChanged(sessionState))
                currentOnEvent(
                    CameraSessionEvent.FlashAvailabilityChanged(
                        boundUseCases.camera.cameraInfo.hasFlashUnit(),
                    ),
                )

                awaitCancellation()
            } finally {
                provider.unbind(
                    boundUseCases.preview,
                    boundUseCases.imageCapture,
                )
            }
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Timber.e(throwable)
        } finally {
            camera = null
            imageCapture = null
            sessionState = CameraSessionState()
            currentOnEvent(CameraSessionEvent.StateChanged(sessionState))
            currentOnEvent(CameraSessionEvent.FlashAvailabilityChanged(false))
        }
    }

    // ImageCapture가 준비된 이후에만 사진 촬영 순간에 사용할 플래시 모드를 반영합니다.
    LaunchedEffect(camera, imageCapture, isFlashEnabled) {
        val boundCamera = camera ?: return@LaunchedEffect
        val boundImageCapture = imageCapture ?: return@LaunchedEffect
        val shouldEnableFlash = isFlashEnabled && boundCamera.cameraInfo.hasFlashUnit()

        boundImageCapture.flashMode =
            if (shouldEnableFlash) {
                ImageCapture.FLASH_MODE_ON
            } else {
                ImageCapture.FLASH_MODE_OFF
            }
    }

    // 기기가 지원하는 줌 범위를 벗어나지 않도록 실제 적용 비율을 제한합니다.
    LaunchedEffect(camera, zoomLevel) {
        val boundCamera = camera ?: return@LaunchedEffect
        val zoomState = boundCamera.cameraInfo.zoomState.value ?: return@LaunchedEffect
        val supportedZoomRatio =
            zoomLevel
                .toFloat()
                .coerceIn(zoomState.minZoomRatio, zoomState.maxZoomRatio)

        try {
            boundCamera.cameraControl.setZoomRatio(supportedZoomRatio).await()
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Failed to update camera zoom")
        }
    }

    // requestId가 바뀐 경우에만 새로운 촬영 요청으로 소비합니다.
    LaunchedEffect(captureRequest?.requestId) {
        val request = captureRequest ?: return@LaunchedEffect
        val boundImageCapture = imageCapture

        if (boundImageCapture == null || sessionState.isCapturing) {
            currentOnEvent(
                CameraSessionEvent.PhotoCaptureResult(
                    roomId = request.roomId,
                    succeeded = false,
                ),
            )
            return@LaunchedEffect
        }

        sessionState = sessionState.copy(isCapturing = true)
        currentOnEvent(CameraSessionEvent.StateChanged(sessionState))
        val succeeded =
            try {
                boundImageCapture
                    .takePicture(
                        onCaptureStarted = {
                            currentOnEvent(CameraSessionEvent.CaptureStarted)
                        },
                    )
                    .close()
                true
            } catch (cancellationException: CancellationException) {
                currentOnEvent(CameraSessionEvent.PhotoCaptureCancelled(request.roomId))
                throw cancellationException
            } catch (exception: ImageCaptureException) {
                Timber.e(exception)
                false
            } finally {
                sessionState = sessionState.copy(isCapturing = false)
                currentOnEvent(CameraSessionEvent.StateChanged(sessionState))
            }

        currentOnEvent(
            CameraSessionEvent.PhotoCaptureResult(
                roomId = request.roomId,
                succeeded = succeeded,
            ),
        )
    }

    AndroidView(
        modifier = modifier,
        factory = { previewView },
    )
}

private fun createPreviewView(context: Context): PreviewView =
    PreviewView(context).apply {
        scaleType = PreviewView.ScaleType.FILL_CENTER
    }

private fun bindCameraUseCases(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    lensFacing: CameraLensFacing,
): BoundCameraUseCases {
    val preview = createPreview(previewView)
    val imageCapture = createImageCapture()
    val cameraSelector = createCameraSelector(lensFacing)
    val camera =
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture,
        )

    return BoundCameraUseCases(
        camera = camera,
        preview = preview,
        imageCapture = imageCapture,
    )
}

private fun createImageCapture(): ImageCapture =
    ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()

private fun createPreview(previewView: PreviewView): Preview =
    Preview.Builder()
        .build()
        .also { preview ->
            preview.surfaceProvider = previewView.surfaceProvider
        }

private fun createCameraSelector(lensFacing: CameraLensFacing): CameraSelector =
    CameraSelector.Builder()
        .requireLensFacing(lensFacing.toCameraSelectorLensFacing())
        .build()

private fun CameraLensFacing.toCameraSelectorLensFacing(): Int =
    when (this) {
        CameraLensFacing.BACK -> CameraSelector.LENS_FACING_BACK
        CameraLensFacing.FRONT -> CameraSelector.LENS_FACING_FRONT
    }
