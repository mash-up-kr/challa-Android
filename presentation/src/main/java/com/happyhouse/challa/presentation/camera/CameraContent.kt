package com.happyhouse.challa.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.happyhouse.challa.presentation.camera.camerax.CameraSession
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
 */
@Composable
internal fun CameraContent(
    modifier: Modifier = Modifier,
    state: CameraState,
    permissionState: CameraPermissionState,
    captureRequest: PhotoCaptureRequest?,
    onRequestPermissionClick: () -> Unit,
    onPhotoCaptureResult: (roomId: Long, succeeded: Boolean) -> Unit,
    onIntent: (CameraIntent) -> Unit,
) {
    val selectedRoom = state.selectedRoom
    val remainingCount = selectedRoom?.remainingCount ?: 0
    var cameraSessionState by remember { mutableStateOf(CameraSessionState()) }
    var isShutterEffectVisible by remember { mutableStateOf(false) }
    var isRoomSelectionSheetVisible by remember { mutableStateOf(false) }

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
        isFlashOn = state.isFlashOn,
        isCameraSwitchEnabled = !cameraSessionState.isCapturing,
        shutterEnabled =
            remainingCount > 0 &&
                cameraSessionState.isReady &&
                !cameraSessionState.isCapturing,
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
                    isFlashOn = state.isFlashOn,
                    zoomLevel = state.zoomLevel,
                    captureRequest = captureRequest,
                    onStateChanged = { cameraSessionState = it },
                    onCaptureStarted = { isShutterEffectVisible = true },
                    onPhotoCaptureResult = onPhotoCaptureResult,
                    onFlashAvailabilityChanged = {
                        onIntent(CameraIntent.FlashAvailabilityChanged(it))
                    },
                    onFlashStateChanged = {
                        onIntent(CameraIntent.FlashStateChanged(it))
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
