package com.happyhouse.challa.presentation.camera

import androidx.camera.core.ImageCapture
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.happyhouse.challa.presentation.camera.camerax.CameraSession
import com.happyhouse.challa.presentation.camera.camerax.capturePhoto
import com.happyhouse.challa.presentation.camera.component.CameraContentLayout
import com.happyhouse.challa.presentation.camera.component.room.CameraRoomSelectionBottomSheet
import com.happyhouse.challa.presentation.camera.contract.CameraIntent
import com.happyhouse.challa.presentation.camera.contract.CameraState
import com.happyhouse.challa.presentation.camera.model.PhotoCaptureRequest
import com.happyhouse.challa.presentation.camera.permission.CameraPermissionOverlay
import com.happyhouse.challa.presentation.camera.permission.CameraPermissionState
import kotlinx.coroutines.delay
import androidx.camera.core.Camera as CameraXCamera

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
    val context = LocalContext.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val selectedRoom = state.selectedRoom
    val remainingCount = selectedRoom?.remainingCount ?: 0
    var camera by remember { mutableStateOf<CameraXCamera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var isShutterEffectVisible by remember { mutableStateOf(false) }
    var isRoomSelectionSheetVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isShutterEffectVisible) {
        if (isShutterEffectVisible) {
            delay(SHUTTER_EFFECT_DURATION_MILLIS)
            isShutterEffectVisible = false
        }
    }

    LaunchedEffect(camera, state.isFlashOn, state.hasFlashUnit) {
        camera?.cameraControl?.enableTorch(state.isFlashOn && state.hasFlashUnit)
    }

    LaunchedEffect(camera, state.zoomLevel) {
        val boundCamera = camera ?: return@LaunchedEffect
        val zoomState = boundCamera.cameraInfo.zoomState.value ?: return@LaunchedEffect
        val supportedZoomRatio =
            state.zoomLevel
                .toFloat()
                .coerceIn(zoomState.minZoomRatio, zoomState.maxZoomRatio)

        boundCamera.cameraControl.setZoomRatio(supportedZoomRatio)
    }

    LaunchedEffect(captureRequest?.requestId) {
        val request = captureRequest ?: return@LaunchedEffect
        val boundImageCapture = imageCapture

        if (boundImageCapture == null || isCapturing) {
            onPhotoCaptureResult(request.roomId, false)
            return@LaunchedEffect
        }

        isCapturing = true
        isShutterEffectVisible = true
        boundImageCapture.capturePhoto(
            executor = mainExecutor,
            onCaptureResult = { succeeded ->
                isCapturing = false
                onPhotoCaptureResult(request.roomId, succeeded)
            },
        )
    }

    CameraContentLayout(
        modifier = modifier,
        roomName = selectedRoom?.name.orEmpty(),
        remainingCount = remainingCount,
        totalCount = selectedRoom?.totalCount ?: 0,
        filterCount = state.filterCount,
        selectedFilterIndex = state.selectedFilterIndex,
        isFlashOn = state.isFlashOn,
        shutterEnabled = remainingCount > 0 && imageCapture != null && !isCapturing,
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
                    onCameraBound = { camera = it },
                    onImageCaptureBound = { imageCapture = it },
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
