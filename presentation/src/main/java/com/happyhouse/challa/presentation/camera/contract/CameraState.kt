package com.happyhouse.challa.presentation.camera.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import com.happyhouse.challa.presentation.camera.model.CameraFilterUiModel
import com.happyhouse.challa.presentation.camera.model.CameraLensFacing
import com.happyhouse.challa.presentation.camera.model.CameraRoomUiModel
import com.happyhouse.challa.presentation.camera.model.CapturedImage
import com.happyhouse.challa.presentation.camera.model.PhotoCaptureRequest
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * 카메라 화면에서 유지하는 UI 상태입니다.
 *
 * @property onboardingState 카메라 온보딩 완료 여부 조회 및 노출 상태
 * @property captureRequest 촬영부터 저장 및 Gallery 전환까지 유지하는 요청.
 * 요청 전이나 촬영·저장 실패 또는 촬영 취소 시에는 null입니다.
 * @property capturedImage 중앙 이동 연출에 표시할 촬영 JPEG. 화면 재생성 시에도 유지하며,
 * 촬영 결과를 받기 전이나 실패·취소 시에는 null입니다.
 * @property isPhotoSaved 이미지 업로드와 사진 등록 API가 모두 성공했는지 여부.
 * Route는 이 값과 해당 요청의 애니메이션 완료 여부를 함께 확인해 Gallery로 이동합니다.
 * @property isFilterSelectorReady 필터 목록 요청이 끝나 선택 UI를 표시할 수 있는지 여부
 * @property selectedFilterIndex [cameraFilters]에서 선택한 필터의 인덱스
 * @property isCapturePending 촬영·저장·화면 전환 중인 요청이 있어 추가 촬영을 막아야 하는지 여부
 * @property selectedFilter 인덱스가 유효하지 않으면 [CameraFilterUiModel.Original]로 복구한 선택 필터
 */
@Immutable
data class CameraState(
    val selectedRoomId: Long = 0L,
    val onboardingState: CameraOnboardingState = CameraOnboardingState.LOADING,
    val roomLoadState: CameraRoomLoadState = CameraRoomLoadState.LOADING,
    val lensFacing: CameraLensFacing = CameraLensFacing.BACK,
    val isFlashEnabled: Boolean = false,
    val captureRequest: PhotoCaptureRequest? = null,
    val capturedImage: CapturedImage? = null,
    val isPhotoSaved: Boolean = false,
    val zoomLevel: Float = 1f,
    val isFilterSelectorReady: Boolean = false,
    val selectedFilterIndex: Int = 0,
    val cameraFilters: ImmutableList<CameraFilterUiModel> =
        persistentListOf(CameraFilterUiModel.Original),
    val rooms: ImmutableList<CameraRoomUiModel> = persistentListOf(),
) : UiState {
    val isCapturePending: Boolean
        get() = captureRequest != null

    val selectedRoom: CameraRoomUiModel?
        get() = rooms.firstOrNull { it.id == selectedRoomId }

    val selectedFilter: CameraFilterUiModel
        get() = cameraFilters.getOrElse(selectedFilterIndex) { CameraFilterUiModel.Original }
}

enum class CameraOnboardingState {
    LOADING,
    REQUIRED,
    COMPLETED,
    LOAD_FAILED,
}

enum class CameraRoomLoadState {
    LOADING,
    LOADED,
    FAILED,
}
