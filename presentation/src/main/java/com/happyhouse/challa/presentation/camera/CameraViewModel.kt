package com.happyhouse.challa.presentation.camera

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.camera.contract.CameraIntent
import com.happyhouse.challa.presentation.camera.contract.CameraLensFacing
import com.happyhouse.challa.presentation.camera.contract.CameraSideEffect
import com.happyhouse.challa.presentation.camera.contract.CameraState
import com.happyhouse.challa.presentation.camera.model.CameraFilter
import com.happyhouse.challa.presentation.camera.model.CameraRoomUiModel
import com.happyhouse.challa.presentation.camera.model.PhotoCaptureRequest
import com.happyhouse.challa.presentation.camera.model.toUiModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = CameraViewModel.Factory::class)
class CameraViewModel @AssistedInject constructor(
    @Assisted private val roomId: Long,
    private val roomRepository: RoomRepository,
) : BaseViewModel<CameraState, CameraIntent, CameraSideEffect>(
        initialState =
            CameraState(
                selectedRoomId = roomId,
            ),
    ) {
    private var nextCaptureRequestId = 0L

    init {
        onIntent(CameraIntent.FetchData(roomId))
    }

    override fun onIntent(intent: CameraIntent) {
        when (intent) {
            is CameraIntent.FetchData -> fetchData(intent.roomId)
            is CameraIntent.FlashClick -> handleFlashClick(intent.isAvailable)
            CameraIntent.SwitchCameraClick -> handleSwitchCameraClick()
            CameraIntent.ShutterClick -> handleShutterClick()
            CameraIntent.ZoomClick -> handleZoomClick()
            is CameraIntent.RoomClick -> handleRoomClick(intent.room)
            is CameraIntent.FilterClick -> handleFilterClick(intent.index)
        }
    }

    private fun fetchData(roomId: Long) {
        viewModelScope.launch {
            when (val result = roomRepository.getRooms()) {
                is ChallaResult.Success -> {
                    val rooms = result.data.map { it.toUiModel() }.toPersistentList()
                    val selectedRoomId =
                        rooms.firstOrNull { it.id == roomId }?.id ?: rooms.first().id

                    updateState {
                        copy(
                            selectedRoomId = selectedRoomId,
                            rooms = rooms,
                        )
                    }
                }

                is ChallaResult.Failure -> {
                    Timber.e("방 목록을 불러오지 못했습니다: $result")
                    sendEffect(CameraSideEffect.RoomLoadFailed)
                }
            }
        }
    }

    private fun handleFlashClick(isAvailable: Boolean) {
        if (!isAvailable) {
            viewModelScope.launch {
                sendEffect(CameraSideEffect.FlashNotAvailable)
            }
            return
        }

        updateState {
            copy(isFlashEnabled = !isFlashEnabled)
        }
    }

    private fun handleSwitchCameraClick() {
        updateState {
            copy(
                lensFacing =
                    when (lensFacing) {
                        CameraLensFacing.BACK -> CameraLensFacing.FRONT
                        CameraLensFacing.FRONT -> CameraLensFacing.BACK
                    },
                isFlashEnabled = false,
            )
        }
    }

    private fun handleShutterClick() {
        if (currentState.isCapturePending) return

        val room =
            currentState.selectedRoom ?: run {
                Timber.w("선택된 방이 없어 촬영 요청을 무시합니다")
                return
            }
        if (room.remainingCount <= 0) return

        nextCaptureRequestId += 1
        val captureRequest =
            PhotoCaptureRequest(
                requestId = nextCaptureRequestId,
                roomId = room.id,
                selectedFilter = currentState.selectedFilter,
                lensFacing = currentState.lensFacing,
            )
        updateState { copy(captureRequest = captureRequest) }
    }

    fun onPhotoCaptureResult(
        requestId: Long,
        succeeded: Boolean,
    ) {
        val captureRequest =
            currentState.captureRequest?.takeIf { it.requestId == requestId } ?: return

        if (!succeeded) {
            updateState { copy(captureRequest = null) }
            viewModelScope.launch {
                sendEffect(CameraSideEffect.PhotoCaptureFailed)
            }
            return
        }

        // TODO: 실제 연동 시 CameraX 촬영 성공이 아니라 사진 업로드 성공 응답을 기준으로 갱신합니다.
        updateState {
            copy(
                captureRequest = null,
                rooms =
                    rooms
                        .map { room ->
                            if (room.id == captureRequest.roomId && room.remainingCount > 0) {
                                room.copy(remainingCount = room.remainingCount - 1)
                            } else {
                                room
                            }
                        }.toPersistentList(),
            )
        }
    }

    /** CameraX 촬영이 취소됐음을 반영하고 대기 중 상태를 해제합니다. */
    fun onPhotoCaptureCancelled(requestId: Long) {
        if (currentState.captureRequest?.requestId != requestId) return

        updateState { copy(captureRequest = null) }
    }

    private fun handleZoomClick() {
        updateState {
            copy(
                zoomLevel =
                    when (zoomLevel) {
                        DEFAULT_ZOOM_LEVEL -> DOUBLE_ZOOM_LEVEL
                        DOUBLE_ZOOM_LEVEL -> TRIPLE_ZOOM_LEVEL
                        else -> DEFAULT_ZOOM_LEVEL
                    },
            )
        }
    }

    private fun handleRoomClick(room: CameraRoomUiModel) {
        updateState {
            copy(selectedRoomId = room.id)
        }
    }

    private fun handleFilterClick(index: Int) {
        updateState {
            copy(selectedFilterIndex = index.coerceIn(0, CameraFilter.availableFilters.lastIndex))
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(roomId: Long): CameraViewModel
    }

    private companion object {
        const val DEFAULT_ZOOM_LEVEL = 1f
        const val DOUBLE_ZOOM_LEVEL = 2f
        const val TRIPLE_ZOOM_LEVEL = 3f
    }
}
