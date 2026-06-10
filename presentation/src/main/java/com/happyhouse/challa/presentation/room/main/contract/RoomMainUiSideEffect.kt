package com.happyhouse.challa.presentation.room.main.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface RoomMainUiSideEffect : UiSideEffect {
    data object ShareInviteLink : RoomMainUiSideEffect

    data object NavigateToCamera : RoomMainUiSideEffect

    data object NavigateToGallery : RoomMainUiSideEffect
}
