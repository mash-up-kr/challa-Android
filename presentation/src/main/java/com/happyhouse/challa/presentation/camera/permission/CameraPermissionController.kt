package com.happyhouse.challa.presentation.camera.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 카메라 권한 상태와 현재 상태에 맞는 권한 요청 동작을 제공합니다.
 *
 * 최초 진입 시 시스템 권한 요청을 실행합니다. 사용자가 영구 거부하여 시스템 다이얼로그를
 * 다시 표시할 수 없으면 [CameraPermissionController.requestPermission]이 앱 설정 화면을 엽니다.
 * 설정 화면에서 돌아오면 권한 보유 여부를 다시 확인합니다.
 */
@Composable
fun rememberCameraPermissionController(): CameraPermissionController {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var permissionState by remember { mutableStateOf<CameraPermissionState>(CameraPermissionState.Unchecked) }
    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }
    val settingsLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) {
            permissionState =
                resolveCameraPermissionState(
                    isGranted = context.hasCameraPermission(),
                    hasRequestedPermission = hasRequestedPermission,
                    shouldShowRationale = activity.shouldShowCameraPermissionRationale(),
                )
        }
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            permissionState =
                resolveCameraPermissionState(
                    isGranted = isGranted,
                    hasRequestedPermission = hasRequestedPermission,
                    shouldShowRationale = activity.shouldShowCameraPermissionRationale(),
                )
        }

    LaunchedEffect(context) {
        val isGranted = context.hasCameraPermission()
        val shouldShowRationale = activity.shouldShowCameraPermissionRationale()
        val hasPreviouslyRequestedPermission = hasRequestedPermission || shouldShowRationale

        hasRequestedPermission = hasPreviouslyRequestedPermission
        permissionState =
            resolveCameraPermissionState(
                isGranted = isGranted,
                hasRequestedPermission = hasPreviouslyRequestedPermission,
                shouldShowRationale = shouldShowRationale,
            )

        if (!isGranted && !hasPreviouslyRequestedPermission) {
            hasRequestedPermission = true
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    return CameraPermissionController(
        state = permissionState,
        requestPermission = {
            val currentPermissionState =
                resolveCameraPermissionState(
                    isGranted = context.hasCameraPermission(),
                    hasRequestedPermission = hasRequestedPermission,
                    shouldShowRationale = activity.shouldShowCameraPermissionRationale(),
                )
            permissionState = currentPermissionState

            if (currentPermissionState == CameraPermissionState.PermanentlyDenied) {
                settingsLauncher.launch(context.cameraPermissionSettingsIntent())
            } else if (currentPermissionState != CameraPermissionState.Granted) {
                hasRequestedPermission = true
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        },
    )
}

/**
 * 권한 UI가 관찰하는 상태와 버튼 동작입니다.
 *
 * [requestPermission]은 [state]에 따라 시스템 권한 다이얼로그 또는 앱 설정을 엽니다.
 */
data class CameraPermissionController(
    val state: CameraPermissionState,
    val requestPermission: () -> Unit,
)

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA,
    ) == PackageManager.PERMISSION_GRANTED

private fun Activity?.shouldShowCameraPermissionRationale(): Boolean =
    this != null &&
        ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.CAMERA,
        )

internal fun resolveCameraPermissionState(
    isGranted: Boolean,
    hasRequestedPermission: Boolean,
    shouldShowRationale: Boolean,
): CameraPermissionState =
    when {
        isGranted -> CameraPermissionState.Granted
        hasRequestedPermission && !shouldShowRationale -> CameraPermissionState.PermanentlyDenied
        else -> CameraPermissionState.NotGranted
    }

private fun Context.cameraPermissionSettingsIntent(): Intent =
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null),
    )
