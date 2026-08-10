package com.happyhouse.challa.presentation.camera

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.repository.CameraRepository
import com.happyhouse.challa.domain.repository.ImageUploadRepository
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.camera.contract.CameraIntent
import com.happyhouse.challa.presentation.camera.contract.CameraRoomLoadState
import com.happyhouse.challa.presentation.camera.contract.CameraSideEffect
import com.happyhouse.challa.presentation.camera.contract.CameraState
import com.happyhouse.challa.presentation.camera.model.CameraFilterUiModel
import com.happyhouse.challa.presentation.camera.model.CameraLensFacing
import com.happyhouse.challa.presentation.camera.model.CameraRoomUiModel
import com.happyhouse.challa.presentation.camera.model.PhotoCaptureRequest
import com.happyhouse.challa.presentation.camera.model.remainingCaptureStatus
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
    @Assisted roomId: Long,
    private val cameraRepository: CameraRepository,
    private val imageUploadRepository: ImageUploadRepository,
    private val roomRepository: RoomRepository,
) : BaseViewModel<CameraState, CameraIntent, CameraSideEffect>(
        initialState = CameraState(selectedRoomId = roomId),
    ) {
    private var nextCaptureRequestId = 0L

    init {
        fetchData()
    }

    override fun onIntent(intent: CameraIntent) {
        when (intent) {
            CameraIntent.RoomLoadRetry -> fetchShootableRooms()
            is CameraIntent.FlashClick -> handleFlashClick(intent.isAvailable)
            CameraIntent.SwitchCameraClick -> handleSwitchCameraClick()
            CameraIntent.ShutterClick -> handleShutterClick()
            CameraIntent.ZoomClick -> handleZoomClick()
            is CameraIntent.RoomClick -> handleRoomClick(intent.room)
            is CameraIntent.FilterClick -> handleFilterClick(intent.index)
        }
    }

    private fun fetchData() {
        fetchShootableRooms()
        fetchCameraFilters()
    }

    private fun fetchShootableRooms() {
        val requestedRoomId = currentState.selectedRoomId

        viewModelScope.launch {
            updateState { copy(roomLoadState = CameraRoomLoadState.LOADING) }

            when (val result = roomRepository.getShootableRooms()) {
                is ChallaResult.Success -> {
                    val rooms = result.data.map { it.toUiModel() }.toPersistentList()
                    val selectedRoomId = rooms.firstOrNull { it.id == requestedRoomId }?.id

                    if (selectedRoomId == null) {
                        updateState { copy(roomLoadState = CameraRoomLoadState.FAILED) }
                        Timber.e(
                            "선택할 방을 찾을 수 없습니다: roomId=%d, roomCount=%d",
                            requestedRoomId,
                            rooms.size,
                        )
                        sendEffect(CameraSideEffect.RoomLoadFailed)
                        return@launch
                    }

                    updateState {
                        copy(
                            selectedRoomId = selectedRoomId,
                            roomLoadState = CameraRoomLoadState.LOADED,
                            rooms = rooms,
                        )
                    }
                }

                is ChallaResult.Failure -> {
                    updateState { copy(roomLoadState = CameraRoomLoadState.FAILED) }
                    Timber.e("방 목록을 불러오지 못했습니다: $result")
                    sendEffect(CameraSideEffect.RoomLoadFailed)
                }
            }
        }
    }

    private fun fetchCameraFilters() {
        viewModelScope.launch {
            when (val result = cameraRepository.getCameraFilters()) {
                is ChallaResult.Success -> {
                    val cameraFilters =
                        (listOf(CameraFilterUiModel.Original) + result.data.map { it.toUiModel() })
                            .toPersistentList()
                    updateState {
                        copy(
                            cameraFilters = cameraFilters,
                            selectedFilterIndex =
                                selectedFilterIndex.coerceIn(
                                    0,
                                    cameraFilters.lastIndex,
                                ),
                        )
                    }
                }

                is ChallaResult.Failure -> {
                    Timber.e("카메라 필터 목록을 불러오지 못했습니다: $result")
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
        if (!room.remainingCaptureStatus.isCaptureAvailable) {
            viewModelScope.launch {
                sendEffect(CameraSideEffect.NoRemainingCaptures)
            }
            return
        }

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

    fun onPhotoCaptured(
        requestId: Long,
        imageBytes: ByteArray,
    ) {
        val captureRequest =
            currentState.captureRequest?.takeIf { it.requestId == requestId } ?: return

        viewModelScope.launch {
            val imageUrl =
                when (val result = imageUploadRepository.uploadPhoto(imageBytes)) {
                    is ChallaResult.Success -> result.data
                    is ChallaResult.Failure -> {
                        handlePhotoCreateFailure(result)
                        return@launch
                    }
                }

            when (
                val result =
                    cameraRepository.postPhoto(
                        roomId = captureRequest.roomId,
                        cameraFilterName = captureRequest.selectedFilter.name,
                        imageUrl = imageUrl,
                    )
            ) {
                is ChallaResult.Success -> completePhotoCreation(captureRequest)
                is ChallaResult.Failure -> handlePhotoCreateFailure(result)
            }
        }
    }

    fun onPhotoCaptureFailed(requestId: Long) {
        if (currentState.captureRequest?.requestId != requestId) return

        updateState { copy(captureRequest = null) }
        viewModelScope.launch {
            sendEffect(CameraSideEffect.PhotoCaptureFailed)
        }
    }

    private fun completePhotoCreation(captureRequest: PhotoCaptureRequest) {
        if (currentState.captureRequest?.requestId != captureRequest.requestId) return

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

    private suspend fun handlePhotoCreateFailure(result: ChallaResult.Failure) {
        Timber.e("사진 업로드 또는 생성에 실패했습니다: %s", result)
        updateState { copy(captureRequest = null) }
        sendEffect(CameraSideEffect.PhotoCaptureFailed)
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
            copy(
                selectedRoomId = room.id,
                selectedFilterIndex = 0,
            )
        }
    }

    private fun handleFilterClick(index: Int) {
        updateState {
            copy(selectedFilterIndex = index.coerceIn(0, cameraFilters.lastIndex))
        }
    }

    suspend fun getCameraFilterFile(fileUrl: String): ByteArray? =
        when (val result = cameraRepository.getCameraFilterFile(fileUrl)) {
            is ChallaResult.Success -> result.data
            is ChallaResult.Failure -> {
                Timber.e("카메라 필터 파일을 불러오지 못했습니다: fileUrl=%s, result=%s", fileUrl, result)
                null
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
