package com.happyhouse.challa.presentation.camera

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.camera.contract.CameraLensFacing
import com.happyhouse.challa.presentation.camera.contract.CameraUiIntent
import com.happyhouse.challa.presentation.camera.contract.CameraUiSideEffect
import com.happyhouse.challa.presentation.camera.contract.CameraUiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CameraViewModel.Factory::class)
class CameraViewModel
    @AssistedInject
    constructor(
        @Assisted private val roomId: Long,
    ) : BaseViewModel<CameraUiState, CameraUiIntent, CameraUiSideEffect>(
            initialState =
                CameraUiState(
                    roomId = roomId,
                ),
        ) {
        init {
            onIntent(CameraUiIntent.FetchData(roomId))
        }

        override fun onIntent(intent: CameraUiIntent) {
            when (intent) {
                is CameraUiIntent.FetchData -> fetchData(intent.roomId)
                CameraUiIntent.FlashClick -> handleFlashClick()
                CameraUiIntent.SwitchCameraClick -> handleSwitchCameraClick()
                CameraUiIntent.ShutterClick -> handleShutterClick()
                is CameraUiIntent.FlashAvailabilityChanged -> handleFlashAvailabilityChanged(intent.isAvailable)
            }
        }

        private fun fetchData(roomId: Long) {
            updateState {
                copy(roomId = roomId)
            }
        }

        private fun handleFlashClick() {
            if (!currentState.hasFlashUnit) {
                sendSideEffect(CameraUiSideEffect.FlashNotAvailable)
                return
            }

            updateState {
                copy(isFlashOn = !isFlashOn)
            }

            sendSideEffect(
                if (currentState.isFlashOn) {
                    CameraUiSideEffect.FlashEnabled
                } else {
                    CameraUiSideEffect.FlashDisabled
                },
            )
        }

        private fun handleSwitchCameraClick() {
            updateState {
                copy(
                    lensFacing =
                        when (lensFacing) {
                            CameraLensFacing.Back -> CameraLensFacing.Front
                            CameraLensFacing.Front -> CameraLensFacing.Back
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

        private fun sendSideEffect(sideEffect: CameraUiSideEffect) {
            viewModelScope.launch {
                sendEffect(sideEffect)
            }
        }

        @AssistedFactory
        interface Factory {
            fun create(roomId: Long): CameraViewModel
        }
    }
