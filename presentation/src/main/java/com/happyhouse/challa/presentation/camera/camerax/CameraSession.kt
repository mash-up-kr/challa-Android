package com.happyhouse.challa.presentation.camera.camerax

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.happyhouse.challa.presentation.camera.contract.CameraLensFacing
import com.happyhouse.challa.presentation.camera.model.CameraFilter
import com.happyhouse.challa.presentation.camera.model.PhotoCaptureRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * [LifecycleCameraController]로 CameraX Preview, 촬영, 렌즈, 플래시, 줌을 관리합니다.
 *
 * [lensFacing]이 변경되면 Controller가 새 렌즈로 UseCase를 다시 바인딩합니다.
 * [bindingRetryKey]가 바뀐 때는 Controller와 PreviewView를 재생성해 초기화부터 재시도합니다.
 * PreviewView의 핀치 줌은 사용하지 않으며, 촬영 플래시와 [zoomLevel]만 Controller API로 적용합니다.
 * Android 13 이상에서는 모든 LUT를 IO 스레드에서 미리 읽고 [selectedFilter]를 프리뷰에 적용합니다.
 * LUT가 준비되기 전에는 기존 필터를 유지해 필터 전환 중 원본 프레임이 노출되지 않게 합니다.
 * 실패한 제어 요청은 기록합니다.
 * 새로운 [captureRequest]가 전달되면 이미지를 메모리로 촬영하고 즉시 닫은 뒤 처리 결과를 [CameraSessionEvent.CaptureCompleted]로 전달합니다.
 * Composable이 Composition에서 제거되면 Controller를 해제하고 진행 중인 촬영 코루틴을 취소합니다.
 *
 * @param captureRequest 현재 세션에서 처리할 촬영 요청. [PhotoCaptureRequest.requestId]가 바뀔 때마다
 * 새 요청으로 처리하며, 호출자는 결과를 받은 뒤 요청을 제거해야 합니다.
 * @param selectedFilter 프리뷰에 적용할 필터
 * @param bindingRetryKey 카메라 초기화·바인딩 실패 후 Controller를 재생성해 재시도할 때 변경하는 키
 * @param onStateChanged 바인딩·촬영 상태가 바뀐 때 최신 [CameraSessionState]를 전달하는 콜백
 * @param onEvent 바인딩 실패와 촬영 시작·완료처럼 한 번만 소비할 [CameraSessionEvent]를 전달하는 콜백
 */
@Composable
internal fun CameraSession(
    modifier: Modifier = Modifier,
    lensFacing: CameraLensFacing,
    isFlashEnabled: Boolean,
    zoomLevel: Float,
    selectedFilter: CameraFilter,
    captureRequest: PhotoCaptureRequest?,
    bindingRetryKey: Int,
    onStateChanged: (CameraSessionState) -> Unit,
    onEvent: (CameraSessionEvent) -> Unit,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember(context, bindingRetryKey) { createCameraController(context) }
    val previewView =
        remember(context, cameraController) { createPreviewView(context, cameraController) }
    val callbackExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val currentOnStateChanged by rememberUpdatedState(onStateChanged)
    val currentOnEvent by rememberUpdatedState(onEvent)
    var sessionState by remember { mutableStateOf(CameraSessionState()) }
    var loadedLuts by remember { mutableStateOf(emptyMap<CameraFilter, Bitmap>()) }

    // 필터 전환 시 디스크 파싱을 기다리지 않도록 모든 cube LUT를 백그라운드에서 미리 읽습니다.
    LaunchedEffect(resources) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@LaunchedEffect

        loadedLuts =
            withContext(Dispatchers.IO) {
                CameraFilter.availableFilters
                    .mapNotNull { filter ->
                        filter.cubeResId?.let { resourceId ->
                            filter to CubeLut.load(resources, resourceId)
                        }
                    }.toMap()
            }
    }

    LaunchedEffect(previewView, selectedFilter, loadedLuts) {
        previewView.applyCameraFilter(selectedFilter, loadedLuts[selectedFilter])
    }

    // Controller 초기화 후 선택한 렌즈를 Lifecycle에 바인딩하고 세션 해제 시 unbind합니다.
    LaunchedEffect(cameraController, lifecycleOwner, previewView, lensFacing) {
        sessionState =
            CameraSessionState(
                bindingState = CameraBindingState.Binding(lensFacing),
            )
        currentOnStateChanged(sessionState)

        try {
            cameraController.initializationFuture.await()

            val cameraSelector = lensFacing.toCameraSelector()
            if (!cameraController.hasCamera(cameraSelector)) {
                throw CameraUnavailableException()
            }

            cameraController.cameraSelector = cameraSelector
            cameraController.bindToLifecycle(lifecycleOwner)
            val cameraInfo =
                checkNotNull(cameraController.cameraInfo) {
                    "Controller 바인딩 후 CameraInfo를 확인할 수 없습니다"
                }

            sessionState =
                CameraSessionState(
                    bindingState =
                        CameraBindingState.Ready(
                            lensFacing = lensFacing,
                            hasFlashUnit = cameraInfo.hasFlashUnit(),
                        ),
                )
            currentOnStateChanged(sessionState)
            awaitCancellation()
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            cameraController.unbind()
            Timber.e(throwable, "카메라 Controller 초기화 또는 바인딩에 실패했습니다")
            val bindingFailure = throwable.toCameraBindingFailure()
            sessionState =
                CameraSessionState(
                    bindingState =
                        CameraBindingState.Failed(
                            lensFacing = lensFacing,
                            reason = bindingFailure,
                        ),
                )
            currentOnStateChanged(sessionState)
            currentOnEvent(CameraSessionEvent.BindingFailed(bindingFailure))
            awaitCancellation()
        } finally {
            cameraController.unbind()
            sessionState = CameraSessionState()
            currentOnStateChanged(sessionState)
        }
    }

    // Controller에 바인딩된 렌즈의 플래시 지원 여부를 확인한 뒤 촬영 플래시를 설정합니다.
    LaunchedEffect(cameraController, sessionState.bindingState, isFlashEnabled) {
        if (!sessionState.isReady) return@LaunchedEffect

        val shouldEnableFlash =
            isFlashEnabled && cameraController.cameraInfo?.hasFlashUnit() == true
        cameraController.imageCaptureFlashMode =
            if (shouldEnableFlash) {
                ImageCapture.FLASH_MODE_ON
            } else {
                ImageCapture.FLASH_MODE_OFF
            }
    }

    // 기기가 지원하는 줌 범위를 벗어나지 않도록 제한한 배율을 Controller에 적용합니다.
    LaunchedEffect(cameraController, sessionState.bindingState, zoomLevel) {
        if (!sessionState.isReady) return@LaunchedEffect

        val zoomState = cameraController.zoomState.value ?: return@LaunchedEffect
        val supportedZoomRatio =
            zoomLevel
                .coerceIn(zoomState.minZoomRatio, zoomState.maxZoomRatio)

        try {
            cameraController.setZoomRatio(supportedZoomRatio).await()
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Timber.e(throwable, "카메라 줌 변경에 실패했습니다")
        }
    }

    // requestId가 바뀐 경우에만 Controller에 새로운 촬영을 요청합니다.
    LaunchedEffect(cameraController, lensFacing, captureRequest?.requestId) {
        val request = captureRequest ?: return@LaunchedEffect

        if (!sessionState.isReady || sessionState.isCapturing) {
            currentOnEvent(
                CameraSessionEvent.CaptureCompleted(
                    requestId = request.requestId,
                    result =
                        CameraCaptureResult.Failed(
                            reason =
                                if (!sessionState.isReady) {
                                    CameraCaptureFailure.SESSION_NOT_READY
                                } else {
                                    CameraCaptureFailure.CAPTURE_ALREADY_RUNNING
                                },
                        ),
                ),
            )
            return@LaunchedEffect
        }

        sessionState = sessionState.copy(isCapturing = true)
        currentOnStateChanged(sessionState)
        val result =
            try {
                cameraController
                    .takePicture(
                        executor = callbackExecutor,
                        onCaptureStarted = {
                            currentOnEvent(
                                CameraSessionEvent.CaptureStarted(request.requestId),
                            )
                        },
                    ).close() // TODO: 필터 처리 및 저장 구현 전까지 촬영 결과를 즉시 해제
                CameraCaptureResult.Success
            } catch (cancellationException: CancellationException) {
                currentOnEvent(
                    CameraSessionEvent.CaptureCompleted(
                        requestId = request.requestId,
                        result = CameraCaptureResult.Cancelled,
                    ),
                )
                throw cancellationException
            } catch (throwable: Throwable) {
                Timber.e(throwable, "사진 촬영에 실패했습니다")
                CameraCaptureResult.Failed(CameraCaptureFailure.CAMERA_ERROR)
            } finally {
                sessionState = sessionState.copy(isCapturing = false)
                currentOnStateChanged(sessionState)
            }

        currentOnEvent(
            CameraSessionEvent.CaptureCompleted(
                requestId = request.requestId,
                result = result,
            ),
        )
    }

    key(previewView) {
        AndroidView(
            modifier = modifier,
            factory = { previewView },
        )
    }
}

private fun createCameraController(context: Context): LifecycleCameraController =
    LifecycleCameraController(context).apply {
        setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        isPinchToZoomEnabled = false
        imageCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
    }

/**
 * View의 RenderEffect가 카메라 프레임에도 적용되도록 TextureView 기반의 PreviewView를 만듭니다.
 * SurfaceView 기반 PERFORMANCE 모드는 프리뷰가 View의 렌더 노드를 우회하므로 사용할 수 없습니다.
 */
private fun createPreviewView(
    context: Context,
    cameraController: LifecycleCameraController,
): PreviewView =
    PreviewView(context).apply {
        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        scaleType = PreviewView.ScaleType.FILL_CENTER
        controller = cameraController
    }

private fun CameraLensFacing.toCameraSelector(): CameraSelector =
    when (this) {
        CameraLensFacing.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
        CameraLensFacing.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
    }

private class CameraUnavailableException : IllegalStateException()

private fun Throwable.toCameraBindingFailure(): CameraBindingFailure =
    when (this) {
        is CameraUnavailableException -> CameraBindingFailure.CAMERA_UNAVAILABLE
        is SecurityException -> CameraBindingFailure.PERMISSION_DENIED
        else -> CameraBindingFailure.BINDING_ERROR
    }
