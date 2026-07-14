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

@Immutable
internal data class CameraSessionState(
    val isReady: Boolean = false,
    val isCapturing: Boolean = false,
)

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

    LaunchedEffect(camera, isFlashOn) {
        val boundCamera = camera ?: return@LaunchedEffect
        boundCamera.cameraControl.enableTorch(
            isFlashOn && boundCamera.cameraInfo.hasFlashUnit(),
        )
    }

    LaunchedEffect(camera, zoomLevel) {
        val boundCamera = camera ?: return@LaunchedEffect
        val zoomState = boundCamera.cameraInfo.zoomState.value ?: return@LaunchedEffect
        val supportedZoomRatio =
            zoomLevel
                .toFloat()
                .coerceIn(zoomState.minZoomRatio, zoomState.maxZoomRatio)

        boundCamera.cameraControl.setZoomRatio(supportedZoomRatio)
    }

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
