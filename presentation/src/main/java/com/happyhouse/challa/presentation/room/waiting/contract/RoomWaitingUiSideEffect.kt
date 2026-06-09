package com.happyhouse.challa.presentation.room.waiting.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface RoomWaitingUiSideEffect : UiSideEffect {
    data object NavigateBack : RoomWaitingUiSideEffect

    data object ShowShareSheet : RoomWaitingUiSideEffect

    data object NavigateToRoom : RoomWaitingUiSideEffect
}
