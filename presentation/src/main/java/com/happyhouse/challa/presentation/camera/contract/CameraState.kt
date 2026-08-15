package com.happyhouse.challa.presentation.camera.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import com.happyhouse.challa.presentation.camera.model.CameraFilterUiModel
import com.happyhouse.challa.presentation.camera.model.CameraLensFacing
import com.happyhouse.challa.presentation.camera.model.CameraRoomUiModel
import com.happyhouse.challa.presentation.camera.model.PhotoCaptureRequest
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * 카메라 화면에서 유지하는 UI 상태입니다.
 *
 * @property captureRequest ViewModel이 생성하고 완료 또는 취소까지 소유하는 촬영 요청.
 * 대기 중인 요청이 없으면 null입니다.
 * @property selectedFilterIndex [cameraFilters]에서 선택한 필터의 인덱스
 * @property isCapturePending 처리할 촬영 요청이 있는지 여부
 * @property selectedFilter 인덱스가 유효하지 않으면 [CameraFilterUiModel.Original]로 복구한 선택 필터
 */
@Immutable
data class CameraState(
    val selectedRoomId: Long = 0L,
    val hasCompletedOnboarding: Boolean? = null,
    val roomLoadState: CameraRoomLoadState = CameraRoomLoadState.LOADING,
    val lensFacing: CameraLensFacing = CameraLensFacing.BACK,
    val isFlashEnabled: Boolean = false,
    val captureRequest: PhotoCaptureRequest? = null,
    val zoomLevel: Float = 1f,
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

enum class CameraRoomLoadState {
    LOADING,
    LOADED,
    FAILED,
}
