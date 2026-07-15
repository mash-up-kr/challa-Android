package com.happyhouse.challa.presentation.camera

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.camera.contract.CameraIntent
import com.happyhouse.challa.presentation.camera.contract.CameraLensFacing
import com.happyhouse.challa.presentation.camera.contract.CameraSideEffect
import com.happyhouse.challa.presentation.camera.contract.CameraState
import com.happyhouse.challa.presentation.camera.model.CameraRoomUiModel
import com.happyhouse.challa.presentation.model.ROOM_REQUIRED_PHOTO_COUNT
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CameraViewModel.Factory::class)
class CameraViewModel @AssistedInject constructor(
    @Assisted private val roomId: Long,
) : BaseViewModel<CameraState, CameraIntent, CameraSideEffect>(
        initialState =
            CameraState(
                selectedRoomId = roomId,
            ),
    ) {
    init {
        onIntent(CameraIntent.FetchData(roomId))
    }

    override fun onIntent(intent: CameraIntent) {
        when (intent) {
            is CameraIntent.FetchData -> fetchData(intent.roomId)
            CameraIntent.FlashClick -> handleFlashClick()
            CameraIntent.SwitchCameraClick -> handleSwitchCameraClick()
            is CameraIntent.ShutterClick -> handleShutterClick(intent.roomId)
            CameraIntent.ZoomClick -> handleZoomClick()
            is CameraIntent.RoomClick -> handleRoomClick(intent.room)
            is CameraIntent.FilterClick -> handleFilterClick(intent.index)
            is CameraIntent.FlashAvailabilityChanged -> handleFlashAvailabilityChanged(intent.isAvailable)
        }
    }

    private fun fetchData(roomId: Long) {
        val rooms = createMockRooms(roomId)
        val selectedRoom = rooms.first()

        updateState {
            copy(
                selectedRoomId = selectedRoom.id,
                rooms = rooms,
            )
        }
    }

    private fun createMockRooms(roomId: Long) =
        persistentListOf(
            CameraRoomUiModel(
                id = roomId,
                name = "방이름1",
                remainingCount = ROOM_REQUIRED_PHOTO_COUNT,
                totalCount = ROOM_REQUIRED_PHOTO_COUNT,
            ),
            CameraRoomUiModel(
                id = roomId + 1,
                name = "방이름방이름방이름2",
                remainingCount = 6,
                totalCount = 24,
            ),
            CameraRoomUiModel(
                id = roomId + 2,
                name = "방이름방이름방이름3방이름",
                remainingCount = 3,
                totalCount = 48,
            ),
            CameraRoomUiModel(
                id = roomId + 3,
                name = "방이름방이름방이름4",
                remainingCount = 3,
                totalCount = 48,
            ),
        )

    private fun handleFlashClick() {
        if (!currentState.hasFlashUnit) {
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

    private fun handleShutterClick(roomId: Long) {
        if (currentState.isCapturePending) return

        val room = currentState.rooms.firstOrNull { it.id == roomId } ?: return
        if (room.remainingCount <= 0) return

        updateState {
            copy(isCapturePending = true)
        }
        viewModelScope.launch {
            sendEffect(CameraSideEffect.PhotoCaptureRequested(roomId))
        }
    }

    fun onPhotoCaptureResult(
        roomId: Long,
        succeeded: Boolean,
    ) {
        if (!succeeded) {
            updateState {
                copy(isCapturePending = false)
            }
            viewModelScope.launch {
                sendEffect(CameraSideEffect.PhotoCaptureFailed)
            }
            return
        }

        updateState {
            copy(
                isCapturePending = false,
                rooms =
                    rooms
                        .map { room ->
                            if (room.id == roomId && room.remainingCount > 0) {
                                room.copy(remainingCount = room.remainingCount - 1)
                            } else {
                                room
                            }
                        }.toPersistentList(),
            )
        }
    }

    fun onPhotoCaptureCancelled() {
        updateState {
            copy(isCapturePending = false)
        }
    }

    private fun handleZoomClick() {
        updateState {
            copy(
                zoomLevel = if (zoomLevel == MAX_ZOOM_LEVEL) MIN_ZOOM_LEVEL else zoomLevel + 1,
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
            copy(selectedFilterIndex = index.coerceIn(0, (filterCount - 1).coerceAtLeast(0)))
        }
    }

    private fun handleFlashAvailabilityChanged(isAvailable: Boolean) {
        updateState {
            copy(
                hasFlashUnit = isAvailable,
                isFlashEnabled = isFlashEnabled && isAvailable,
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(roomId: Long): CameraViewModel
    }

    private companion object {
        const val MIN_ZOOM_LEVEL = 1
        const val MAX_ZOOM_LEVEL = 2
    }
}
