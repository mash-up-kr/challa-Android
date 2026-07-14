package com.happyhouse.challa.presentation.camera.permission

import androidx.compose.runtime.Immutable

/** 카메라 화면에서 사용하는 런타임 권한 상태입니다. */
@Immutable
sealed interface CameraPermissionState {
    /** 앱 진입 직후 아직 권한 보유 여부를 확인하지 않은 상태입니다. */
    data object Unchecked : CameraPermissionState

    /** 카메라 권한이 허용되어 CameraX 세션을 시작할 수 있는 상태입니다. */
    data object Granted : CameraPermissionState

    /** 권한은 없지만 시스템 권한 요청을 다시 표시할 수 있는 상태입니다. */
    data object NotGranted : CameraPermissionState

    /** 시스템 권한 요청을 다시 표시할 수 없어 앱 설정에서 직접 허용해야 하는 상태입니다. */
    data object PermanentlyDenied : CameraPermissionState
}
