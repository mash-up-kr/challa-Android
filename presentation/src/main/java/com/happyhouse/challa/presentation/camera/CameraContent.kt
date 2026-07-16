package com.happyhouse.challa.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.happyhouse.challa.presentation.camera.camerax.CameraSession
import com.happyhouse.challa.presentation.camera.camerax.CameraSessionEvent
import com.happyhouse.challa.presentation.camera.camerax.CameraSessionState
import com.happyhouse.challa.presentation.camera.component.CameraContentLayout
import com.happyhouse.challa.presentation.camera.component.room.CameraRoomSelectionBottomSheet
import com.happyhouse.challa.presentation.camera.contract.CameraIntent
import com.happyhouse.challa.presentation.camera.contract.CameraState
import com.happyhouse.challa.presentation.camera.model.PhotoCaptureRequest
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
 *
 * @param captureRequest [CameraSession]이 소비할 촬영 요청. 대기 중인 요청이 없으면 null
 */
@Composable
internal fun CameraContent(
    modifier: Modifier = Modifier,
    state: CameraState,
    permissionState: CameraPermissionState,
    captureRequest: PhotoCaptureRequest?,
    cameraBindingRetryKey: Int,
    onRequestPermissionClick: () -> Unit,
    onCameraBindingFailed: () -> Unit,
    onPhotoCaptureResult: (roomId: Long, succeeded: Boolean) -> Unit,
    onPhotoCaptureCancelled: () -> Unit,
    onIntent: (CameraIntent) -> Unit,
) {
    val selectedRoom = state.selectedRoom
    val remainingCount = selectedRoom?.remainingCount ?: 0
    var cameraSessionState by remember { mutableStateOf(CameraSessionState()) }
    var isShutterEffectVisible by remember { mutableStateOf(false) }
    var isRoomSelectionSheetVisible by remember { mutableStateOf(false) }
    val isCameraIdle = !state.isCapturePending && !cameraSessionState.isCapturing
    val canControlCamera = cameraSessionState.isReady && isCameraIdle
    val canCapture = remainingCount > 0 && canControlCamera

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
        filterCount = state.filterCount,
        selectedFilterIndex = state.selectedFilterIndex,
        isFlashEnabled = state.isFlashEnabled,
        isCameraSwitchEnabled = canControlCamera,
        shutterEnabled = canCapture,
        isShutterEffectVisible = isShutterEffectVisible,
        zoomLevel = state.zoomLevel,
        onFlashClick = { onIntent(CameraIntent.FlashClick) },
        onSwitchCameraClick = { onIntent(CameraIntent.SwitchCameraClick) },
        onShutterClick = { onIntent(CameraIntent.ShutterClick(state.selectedRoomId)) },
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
                    captureRequest = captureRequest,
                    bindingRetryKey = cameraBindingRetryKey,
                    onEvent = { event ->
                        when (event) {
                            is CameraSessionEvent.StateChanged -> {
                                cameraSessionState = event.state
                            }

                            CameraSessionEvent.BindingFailed -> {
                                onCameraBindingFailed()
                            }

                            CameraSessionEvent.CaptureStarted -> {
                                isShutterEffectVisible = true
                            }

                            is CameraSessionEvent.PhotoCaptureResult -> {
                                onPhotoCaptureResult(event.roomId, event.succeeded)
                            }

                            is CameraSessionEvent.PhotoCaptureCancelled -> {
                                onPhotoCaptureCancelled()
                            }

                            is CameraSessionEvent.FlashAvailabilityChanged -> {
                                onIntent(CameraIntent.FlashAvailabilityChanged(event.isAvailable))
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
