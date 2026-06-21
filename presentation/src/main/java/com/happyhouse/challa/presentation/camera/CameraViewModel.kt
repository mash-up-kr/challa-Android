package com.happyhouse.challa.presentation.camera

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.camera.contract.CameraIntent
import com.happyhouse.challa.presentation.camera.contract.CameraLensFacing
import com.happyhouse.challa.presentation.camera.contract.CameraSideEffect
import com.happyhouse.challa.presentation.camera.contract.CameraUiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CameraViewModel.Factory::class)
class CameraViewModel @AssistedInject constructor(
    @Assisted private val roomId: Long,
) : BaseViewModel<CameraUiState, CameraIntent, CameraSideEffect>(
        initialState =
            CameraUiState(
                roomId = roomId,
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
            CameraIntent.ShutterClick -> handleShutterClick()
            is CameraIntent.FlashAvailabilityChanged -> handleFlashAvailabilityChanged(intent.isAvailable)
        }
    }

    private fun fetchData(roomId: Long) {
        updateState {
            copy(roomId = roomId)
        }
    }

    private fun handleFlashClick() {
        if (!currentState.hasFlashUnit) {
            viewModelScope.launch {
                sendEffect(CameraSideEffect.FlashNotAvailable)
            }
            return
        }

        updateState {
            copy(isFlashOn = !isFlashOn)
        }

        viewModelScope.launch {
            sendEffect(
                if (currentState.isFlashOn) {
                    CameraSideEffect.FlashEnabled
                } else {
                    CameraSideEffect.FlashDisabled
                },
            )
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
                isFlashOn = false,
            )
        }
    }

    private fun handleShutterClick() = Unit

    private fun handleFlashAvailabilityChanged(isAvailable: Boolean) {
        updateState {
            copy(
                hasFlashUnit = isAvailable,
                isFlashOn = isFlashOn && isAvailable,
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(roomId: Long): CameraViewModel
    }
}
