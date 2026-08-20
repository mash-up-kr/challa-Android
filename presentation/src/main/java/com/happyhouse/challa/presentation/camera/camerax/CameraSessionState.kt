package com.happyhouse.challa.presentation.camera.camerax

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.camera.model.CameraFilterUiModel
import com.happyhouse.challa.presentation.camera.model.CameraLensFacing
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf

/**
 * CameraX 세션에서 UI가 알아야 하는 최소 상태입니다.
 *
 * @property bindingState Controller의 초기화 및 Lifecycle 바인딩 상태
 * @property isCapturing 사진 한 장의 촬영 요청을 처리하고 있는지 여부
 * @property previewFilter 현재 프리뷰에 실제로 적용된 필터
 * @property failedFilterUrls LUT 다운로드 또는 파싱에 실패한 원격 필터 URL
 * @property isReady Controller 바인딩이 완료되어 촬영할 수 있는지 여부
 * [CameraBindingState.Ready]가 아니면 false입니다.
 */
@Immutable
internal data class CameraSessionState(
    val bindingState: CameraBindingState = CameraBindingState.Idle,
    val isCapturing: Boolean = false,
    val previewFilter: CameraFilterUiModel = CameraFilterUiModel.Original,
    val failedFilterUrls: PersistentSet<String> = persistentSetOf(),
) {
    val isReady: Boolean
        get() = bindingState is CameraBindingState.Ready
}

/** LifecycleCameraController의 초기화 및 Lifecycle 바인딩 상태입니다. */
@Immutable
internal sealed interface CameraBindingState {
    /** Controller가 Lifecycle에 바인딩되지 않은 초기 또는 세션 해제 상태입니다. */
    data object Idle : CameraBindingState

    /** Controller를 초기화하고 [lensFacing] 렌즈로 Lifecycle에 바인딩하고 있는 상태입니다. */
    data class Binding(
        val lensFacing: CameraLensFacing,
    ) : CameraBindingState

    /**
     * Controller를 [lensFacing] 렌즈로 Lifecycle에 바인딩한 상태입니다.
     *
     * @property hasFlashUnit 바인딩된 렌즈의 플래시 하드웨어 지원 여부
     */
    data class Ready(
        val lensFacing: CameraLensFacing,
        val hasFlashUnit: Boolean,
    ) : CameraBindingState

    /**
     * Controller를 초기화하거나 [lensFacing] 렌즈로 바인딩하지 못한 상태입니다.
     *
     * @property reason 상위 UI가 복구 방법을 결정할 수 있도록 분류한 실패 원인
     */
    data class Failed(
        val lensFacing: CameraLensFacing,
        val reason: CameraBindingFailure,
    ) : CameraBindingState
}

/** CameraX Controller 초기화 또는 Lifecycle 바인딩 실패 원인입니다. */
enum class CameraBindingFailure {
    /** 요청한 렌즈를 기기에서 사용할 수 없습니다. */
    CAMERA_UNAVAILABLE,

    /** 카메라 권한이 없어 Controller를 바인딩할 수 없습니다. */
    PERMISSION_DENIED,

    /** 위 원인을 제외한 CameraX 초기화 또는 Lifecycle 바인딩 오류입니다. */
    BINDING_ERROR,
}
