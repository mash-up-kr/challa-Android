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
import com.happyhouse.challa.presentation.camera.permission.CameraPermissionState
import kotlinx.coroutines.delay

private const val SHUTTER_EFFECT_DURATION_MILLIS = 120L

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
                    modifier = viewFinderModifier,
                    isCheckingPermission = true,
                    onRequestPermissionClick = onRequestPermissionClick,
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
                )
            }

            CameraPermissionState.NotGranted -> {
                CameraPermissionOverlay(
                    modifier = viewFinderModifier,
                    isCheckingPermission = false,
                    onRequestPermissionClick = onRequestPermissionClick,
                )
            }

            CameraPermissionState.PermanentlyDenied -> {
                CameraPermissionOverlay(
                    modifier = viewFinderModifier,
                    isCheckingPermission = false,
                    isPermanentlyDenied = true,
                    onRequestPermissionClick = onRequestPermissionClick,
                )
            }
        }
    }

    if (isRoomSelectionSheetVisible) {
        CameraRoomSelectionBottomSheet(
            rooms = state.rooms,
            selectedRoomId = state.selectedRoomId,
            onRoomClick = { roomId ->
                onIntent(CameraIntent.RoomClick(roomId))
                isRoomSelectionSheetVisible = false
            },
            onDismissRequest = { isRoomSelectionSheetVisible = false },
        )
    }
}
