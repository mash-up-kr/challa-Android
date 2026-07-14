package com.happyhouse.challa.presentation.camera.camerax

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.happyhouse.challa.presentation.camera.contract.CameraLensFacing
import com.happyhouse.challa.presentation.camera.model.PhotoCaptureRequest
import timber.log.Timber
import java.util.concurrent.Executor
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

/**
 * CameraX Preview와 [ImageCapture]의 생성, 바인딩, 제어를 담당합니다.
 *
 * [lensFacing]이 변경되면 기존 UseCase를 해제하고 새 카메라에 다시 바인딩합니다.
 * 플래시와 줌 값은 바인딩된 CameraX 객체에 반영하며, 새로운 [captureRequest]가 전달되면
 * 이미지를 저장하지 않고 메모리에서 한 장 촬영한 뒤 성공 여부만 반환합니다.
 *
 * @param captureRequest 처리할 촬영 요청. 같은 요청의 중복 처리는
 * [PhotoCaptureRequest.requestId]로 구분합니다.
 * @param onStateChanged 카메라 준비 또는 촬영 상태가 변경될 때 호출됩니다.
 * @param onCaptureStarted 촬영이 시작된 순간 호출되며 셔터 UI 효과에 사용됩니다.
 * @param onPhotoCaptureResult 촬영 대상 방 ID와 촬영 성공 여부를 전달합니다.
 * @param onFlashAvailabilityChanged 현재 렌즈의 플래시 지원 여부를 전달합니다.
 */
@Composable
internal fun CameraSession(
    modifier: Modifier = Modifier,
    lensFacing: CameraLensFacing,
    isFlashOn: Boolean,
    zoomLevel: Int,
    captureRequest: PhotoCaptureRequest?,
    onStateChanged: (CameraSessionState) -> Unit,
    onCaptureStarted: () -> Unit,
    onPhotoCaptureResult: (roomId: Long, succeeded: Boolean) -> Unit,
    onFlashAvailabilityChanged: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember(context) { createPreviewView(context) }
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val currentOnStateChanged by rememberUpdatedState(onStateChanged)
    val currentOnCaptureStarted by rememberUpdatedState(onCaptureStarted)
    val currentOnPhotoCaptureResult by rememberUpdatedState(onPhotoCaptureResult)
    val currentOnFlashAvailabilityChanged by rememberUpdatedState(onFlashAvailabilityChanged)
    var camera by remember { mutableStateOf<CameraXCamera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var sessionState by remember { mutableStateOf(CameraSessionState()) }

    // Composable 및 렌즈의 생명주기에 맞춰 CameraX UseCase를 바인딩하고 해제합니다.
    DisposableEffect(context, lifecycleOwner, previewView, lensFacing) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var cameraProvider: ProcessCameraProvider? = null
        var isDisposed = false

        cameraProviderFuture.addListener(
            {
                runCatching {
                    val provider = cameraProviderFuture.get()
                    cameraProvider = provider

                    if (isDisposed) {
                        return@runCatching
                    }

                    provider.unbindAll()

                    val boundImageCapture = createImageCapture()
                    val boundCamera =
                        bindCameraUseCases(
                            cameraProvider = provider,
                            lifecycleOwner = lifecycleOwner,
                            previewView = previewView,
                            lensFacing = lensFacing,
                            imageCapture = boundImageCapture,
                        )
                    camera = boundCamera
                    imageCapture = boundImageCapture
                    sessionState = sessionState.copy(isReady = true)
                    currentOnStateChanged(sessionState)
                    currentOnFlashAvailabilityChanged(boundCamera.cameraInfo.hasFlashUnit())
                }.onFailure { throwable ->
                    camera = null
                    imageCapture = null
                    sessionState = CameraSessionState()
                    currentOnStateChanged(sessionState)
                    currentOnFlashAvailabilityChanged(false)
                    Timber.e(throwable)
                }
            },
            mainExecutor,
        )

        onDispose {
            isDisposed = true
            camera = null
            imageCapture = null
            sessionState = CameraSessionState()
            currentOnStateChanged(sessionState)
            currentOnFlashAvailabilityChanged(false)
            cameraProvider?.unbindAll()
        }
    }

    // CameraX 객체가 준비된 이후에만 현재 UI의 플래시 상태를 반영합니다.
    LaunchedEffect(camera, isFlashOn) {
        val boundCamera = camera ?: return@LaunchedEffect
        boundCamera.cameraControl.enableTorch(
            isFlashOn && boundCamera.cameraInfo.hasFlashUnit(),
        )
    }

    // 기기가 지원하는 줌 범위를 벗어나지 않도록 실제 적용 비율을 제한합니다.
    LaunchedEffect(camera, zoomLevel) {
        val boundCamera = camera ?: return@LaunchedEffect
        val zoomState = boundCamera.cameraInfo.zoomState.value ?: return@LaunchedEffect
        val supportedZoomRatio =
            zoomLevel
                .toFloat()
                .coerceIn(zoomState.minZoomRatio, zoomState.maxZoomRatio)

        boundCamera.cameraControl.setZoomRatio(supportedZoomRatio)
    }

    // requestId가 바뀐 경우에만 새로운 촬영 요청으로 소비합니다.
    LaunchedEffect(captureRequest?.requestId) {
        val request = captureRequest ?: return@LaunchedEffect
        val boundImageCapture = imageCapture

        if (boundImageCapture == null || sessionState.isCapturing) {
            currentOnPhotoCaptureResult(request.roomId, false)
            return@LaunchedEffect
        }

        sessionState = sessionState.copy(isCapturing = true)
        currentOnStateChanged(sessionState)
        currentOnCaptureStarted()
        boundImageCapture.capturePhoto(
            executor = mainExecutor,
            onCaptureResult = { succeeded ->
                sessionState = sessionState.copy(isCapturing = false)
                currentOnStateChanged(sessionState)
                currentOnPhotoCaptureResult(request.roomId, succeeded)
            },
        )
    }

    ViewFinder(modifier = modifier) { previewModifier ->
        AndroidView(
            modifier = previewModifier,
            factory = { previewView },
        )
    }
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
    imageCapture: ImageCapture,
): CameraXCamera {
    val preview = createPreview(previewView)
    val cameraSelector = createCameraSelector(lensFacing)

    return cameraProvider.bindToLifecycle(
        lifecycleOwner,
        cameraSelector,
        preview,
        imageCapture,
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

/**
 * 사진을 파일로 저장하지 않고 메모리로 촬영합니다.
 *
 * CameraX가 전달한 [ImageProxy]는 성공 즉시 닫아 버퍼가 고갈되지 않도록 합니다.
 */
private fun ImageCapture.capturePhoto(
    executor: Executor,
    onCaptureResult: (Boolean) -> Unit,
) {
    takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                image.close()
                onCaptureResult(true)
            }

            override fun onError(exception: ImageCaptureException) {
                Timber.e(exception)
                onCaptureResult(false)
            }
        },
    )
}
