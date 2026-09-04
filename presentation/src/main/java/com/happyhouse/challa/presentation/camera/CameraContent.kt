package com.happyhouse.challa.presentation.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.happyhouse.challa.presentation.camera.camerax.CameraBindingFailure
import com.happyhouse.challa.presentation.camera.camerax.CameraBindingState
import com.happyhouse.challa.presentation.camera.camerax.CameraCaptureResult
import com.happyhouse.challa.presentation.camera.camerax.CameraSession
import com.happyhouse.challa.presentation.camera.camerax.CameraSessionEvent
import com.happyhouse.challa.presentation.camera.camerax.CameraSessionState
import com.happyhouse.challa.presentation.camera.component.CameraCaptureTransition
import com.happyhouse.challa.presentation.camera.component.CameraContentLayout
import com.happyhouse.challa.presentation.camera.contract.CameraIntent
import com.happyhouse.challa.presentation.camera.contract.CameraRoomLoadState
import com.happyhouse.challa.presentation.camera.contract.CameraState
import com.happyhouse.challa.presentation.camera.model.CameraFilterUiModel
import com.happyhouse.challa.presentation.camera.model.remainingCaptureStatus
import com.happyhouse.challa.presentation.camera.permission.CameraPermissionOverlay
import com.happyhouse.challa.presentation.camera.permission.CameraPermissionOverlayState
import com.happyhouse.challa.presentation.camera.permission.CameraPermissionState
import kotlinx.coroutines.delay

private const val SHUTTER_EFFECT_DURATION_MILLIS = 120L

/**
 * 카메라 화면의 UI 상태와 사용자 이벤트를 조정합니다.
 *
 * CameraX 객체와 촬영 과정은 [CameraSession]에 위임하고, 이 함수는 세션이 전달한
 * 준비·촬영 상태로 셔터 활성화를 결정합니다. 셔터 암전처럼 화면 표현에만
 * 필요한 상태는 여기에서 관리합니다.
 */
@Composable
internal fun CameraContent(
    modifier: Modifier = Modifier,
    state: CameraState,
    permissionState: CameraPermissionState,
    isOnboardingVisible: Boolean,
    onRequestPermissionClick: () -> Unit,
    onCameraBindingFailed: () -> Unit,
    onPhotoCaptured: (requestId: Long, imageBytes: ByteArray) -> Unit,
    onPhotoCaptureFailed: (requestId: Long) -> Unit,
    onPhotoCaptureCancelled: (requestId: Long) -> Unit,
    onSelectedFilterLutLoadFailed: (fileUrl: String) -> Unit,
    getCameraFilterFile: suspend (String) -> ByteArray?,
    onIntent: (CameraIntent) -> Unit,
    onCloseClick: () -> Unit,
    onCaptureAnimationFinished: () -> Unit,
) {
    val captureRequest = state.captureRequest
    val selectedRoom = state.selectedRoom
    val isRoomLoaded = state.roomLoadState == CameraRoomLoadState.LOADED && selectedRoom != null
    val remainingCount = selectedRoom?.remainingCount ?: 0
    var cameraSessionState by remember { mutableStateOf(CameraSessionState()) }
    var isShutterEffectVisible by remember { mutableStateOf(false) }

    val readyState = cameraSessionState.bindingState as? CameraBindingState.Ready
    val isCameraIdle = !state.isCapturePending && !cameraSessionState.isCapturing
    val canControlCamera = readyState?.lensFacing == state.lensFacing && isCameraIdle
    val isSelectedFilterReady =
        state.selectedFilter == CameraFilterUiModel.Original ||
            cameraSessionState.previewFilter == state.selectedFilter
    val failedSelectedFilter =
        (state.selectedFilter as? CameraFilterUiModel.Remote)?.takeIf { filter ->
            filter.fileUrl in cameraSessionState.failedFilterUrls
        }
    val canCapture =
        canControlCamera &&
            isSelectedFilterReady &&
            state.roomLoadState == CameraRoomLoadState.LOADED &&
            selectedRoom?.remainingCaptureStatus?.isCaptureAvailable == true
    val canSwitchCamera =
        canControlCamera ||
            (cameraSessionState.bindingState as? CameraBindingState.Failed)?.reason ==
            CameraBindingFailure.CAMERA_UNAVAILABLE

    LaunchedEffect(isShutterEffectVisible) {
        if (isShutterEffectVisible) {
            delay(SHUTTER_EFFECT_DURATION_MILLIS)
            isShutterEffectVisible = false
        }
    }

    LaunchedEffect(failedSelectedFilter?.fileUrl) {
        failedSelectedFilter?.let { filter ->
            onSelectedFilterLutLoadFailed(filter.fileUrl)
        }
    }

    LaunchedEffect(state.capturedImage, cameraSessionState.failedFilterUrls) {
        val filter = captureRequest?.selectedFilter as? CameraFilterUiModel.Remote
        if (state.capturedImage != null && filter?.fileUrl in cameraSessionState.failedFilterUrls) {
            // 화면 재생성 후 LUT 재로딩 실패가 이미 저장한 사진의 Gallery 이동을 막지 않게 한다.
            onCaptureAnimationFinished()
        }
    }

    Box(modifier = modifier) {
        CameraContentLayout(
            modifier =
                if (state.capturedImage != null) {
                    Modifier.fillMaxSize().alpha(0f).clearAndSetSemantics {}
                } else {
                    Modifier.fillMaxSize()
                },
            remainingCount = remainingCount,
            totalCount = selectedRoom?.totalCount ?: 0,
            isRoomLoaded = isRoomLoaded,
            isFilterSelectorReady = state.isFilterSelectorReady,
            filters = state.cameraFilters,
            selectedFilterIndex = state.selectedFilterIndex,
            isFlashEnabled = state.isFlashEnabled && readyState?.hasFlashUnit == true,
            isCameraSwitchEnabled = canSwitchCamera,
            shutterEnabled = canCapture,
            isShutterEffectVisible = isShutterEffectVisible,
            isOnboardingVisible = isOnboardingVisible,
            zoomLevel = state.zoomLevel,
            onFlashClick = { onIntent(CameraIntent.FlashClick(readyState?.hasFlashUnit == true)) },
            onSwitchCameraClick = { onIntent(CameraIntent.SwitchCameraClick) },
            onShutterClick = { onIntent(CameraIntent.ShutterClick) },
            onZoomClick = { onIntent(CameraIntent.ZoomClick) },
            onFilterClick = { onIntent(CameraIntent.FilterClick(it)) },
            onCloseClick = onCloseClick,
        ) { viewFinderModifier ->
            when (permissionState) {
                CameraPermissionState.Unchecked -> {
                    CameraPermissionOverlay(
                        state = CameraPermissionOverlayState.Checking,
                        onRequestPermissionClick = onRequestPermissionClick,
                        modifier = viewFinderModifier,
                    )
                }

                CameraPermissionState.Granted -> {
                    CameraSession(
                        modifier = viewFinderModifier,
                        lensFacing = state.lensFacing,
                        isFlashEnabled = state.isFlashEnabled,
                        zoomLevel = state.zoomLevel,
                        filters = state.cameraFilters,
                        selectedFilter = captureRequest?.selectedFilter ?: state.selectedFilter,
                        captureRequestId = captureRequest?.requestId.takeIf { state.capturedImage == null },
                        getCameraFilterFile = getCameraFilterFile,
                        onStateChanged = { cameraSessionState = it },
                        onEvent = { event ->
                            when (event) {
                                CameraSessionEvent.BindingFailed -> {
                                    onCameraBindingFailed()
                                }

                                is CameraSessionEvent.CaptureStarted -> {
                                    if (captureRequest?.requestId == event.requestId) {
                                        isShutterEffectVisible = true
                                    }
                                }

                                is CameraSessionEvent.CaptureCompleted -> {
                                    when (event.result) {
                                        is CameraCaptureResult.Success -> {
                                            onPhotoCaptured(event.requestId, event.result.imageBytes)
                                        }

                                        CameraCaptureResult.Failed -> {
                                            onPhotoCaptureFailed(event.requestId)
                                        }

                                        CameraCaptureResult.Cancelled -> {
                                            onPhotoCaptureCancelled(event.requestId)
                                        }
                                    }
                                }
                            }
                        },
                    )
                }

                CameraPermissionState.NotGranted -> {
                    CameraPermissionOverlay(
                        state = CameraPermissionOverlayState.Requestable,
                        onRequestPermissionClick = onRequestPermissionClick,
                        modifier = viewFinderModifier,
                    )
                }

                CameraPermissionState.PermanentlyDenied -> {
                    CameraPermissionOverlay(
                        state = CameraPermissionOverlayState.PermanentlyDenied,
                        onRequestPermissionClick = onRequestPermissionClick,
                        modifier = viewFinderModifier,
                    )
                }
            }
        }
        state.capturedImage?.let { image ->
            CameraCaptureTransition(
                image = image,
                onAnimationFinished = onCaptureAnimationFinished,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
