package com.happyhouse.challa.presentation.camera.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface CameraSideEffect : UiSideEffect {
    data object RoomLoadFailed : CameraSideEffect

    data object FilterListLoadFailed : CameraSideEffect

    data object SelectedFilterLutLoadFailed : CameraSideEffect

    data object PhotoCaptureFailed : CameraSideEffect

    data object FlashNotAvailable : CameraSideEffect

    data object NoRemainingCaptures : CameraSideEffect
}
