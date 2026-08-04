package com.happyhouse.challa.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.happyhouse.challa.presentation.camera.camerax.CameraBindingFailure
import com.happyhouse.challa.presentation.camera.camerax.CameraBindingState
import com.happyhouse.challa.presentation.camera.camerax.CameraCaptureResult
import com.happyhouse.challa.presentation.camera.camerax.CameraSession
import com.happyhouse.challa.presentation.camera.camerax.CameraSessionEvent
import com.happyhouse.challa.presentation.camera.camerax.CameraSessionState
import com.happyhouse.challa.presentation.camera.component.CameraContentLayout
import com.happyhouse.challa.presentation.camera.component.room.CameraRoomSelectionBottomSheet
import com.happyhouse.challa.presentation.camera.contract.CameraIntent
import com.happyhouse.challa.presentation.camera.contract.CameraRoomLoadState
import com.happyhouse.challa.presentation.camera.contract.CameraState
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
 * 준비·촬영 상태로 셔터 활성화를 결정합니다. 셔터 암전과 방 선택 시트처럼 화면 표현에만
 * 필요한 상태는 여기에서 관리합니다.
 */
@Composable
internal fun CameraContent(
    modifier: Modifier = Modifier,
    state: CameraState,
    permissionState: CameraPermissionState,
    cameraBindingRetryKey: Int,
    onRequestPermissionClick: () -> Unit,
    onCameraBindingFailed: (CameraBindingFailure) -> Unit,
    onPhotoCaptureResult: (requestId: Long, succeeded: Boolean) -> Unit,
    onPhotoCaptureCancelled: (requestId: Long) -> Unit,
    getCameraFilterFile: suspend (String) -> ByteArray?,
    onIntent: (CameraIntent) -> Unit,
) {
    val captureRequest = state.captureRequest
    val selectedRoom = state.selectedRoom
    val remainingCount = selectedRoom?.remainingCount ?: 0
    var cameraSessionState by remember { mutableStateOf(CameraSessionState()) }
    var isShutterEffectVisible by remember { mutableStateOf(false) }
    var isRoomSelectionSheetVisible by remember { mutableStateOf(false) }
    val isCameraIdle = !state.isCapturePending && !cameraSessionState.isCapturing
    val canControlCamera =
        cameraSessionState.isReady &&
            cameraSessionState.boundLensFacing == state.lensFacing &&
            isCameraIdle
    val canCapture =
        canControlCamera &&
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

    CameraContentLayout(
        modifier = modifier,
        roomName = selectedRoom?.name.orEmpty(),
        remainingCount = remainingCount,
        totalCount = selectedRoom?.totalCount ?: 0,
        filters = state.cameraFilters,
        selectedFilterIndex = state.selectedFilterIndex,
        isFlashEnabled = state.isFlashEnabled && cameraSessionState.hasFlashUnit,
        isCameraSwitchEnabled = canSwitchCamera,
        shutterEnabled = canCapture,
        isShutterEffectVisible = isShutterEffectVisible,
        zoomLevel = state.zoomLevel,
        onFlashClick = {
            onIntent(CameraIntent.FlashClick(cameraSessionState.hasFlashUnit))
        },
        onSwitchCameraClick = { onIntent(CameraIntent.SwitchCameraClick) },
        onShutterClick = { onIntent(CameraIntent.ShutterClick) },
        onZoomClick = { onIntent(CameraIntent.ZoomClick) },
        onFilterClick = { onIntent(CameraIntent.FilterClick(it)) },
        onRoomInfoClick = { isRoomSelectionSheetVisible = true },
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
                    selectedFilter = state.selectedFilter,
                    captureRequest = captureRequest,
                    bindingRetryKey = cameraBindingRetryKey,
                    getCameraFilterFile = getCameraFilterFile,
                    onStateChanged = { cameraSessionState = it },
                    onEvent = { event ->
                        when (event) {
                            is CameraSessionEvent.BindingFailed -> {
                                onCameraBindingFailed(event.reason)
                            }

                            is CameraSessionEvent.CaptureStarted -> {
                                if (captureRequest?.requestId == event.requestId) {
                                    isShutterEffectVisible = true
                                }
                            }

                            is CameraSessionEvent.CaptureCompleted -> {
                                when (event.result) {
                                    CameraCaptureResult.Success -> {
                                        onPhotoCaptureResult(event.requestId, true)
                                    }

                                    is CameraCaptureResult.Failed -> {
                                        onPhotoCaptureResult(event.requestId, false)
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

    if (isRoomSelectionSheetVisible) {
        CameraRoomSelectionBottomSheet(
            rooms = state.rooms,
            selectedRoomId = state.selectedRoomId,
            onRoomClick = { room ->
                onIntent(CameraIntent.RoomClick(room))
                isRoomSelectionSheetVisible = false
            },
            onDismissRequest = { isRoomSelectionSheetVisible = false },
        )
    }
}
