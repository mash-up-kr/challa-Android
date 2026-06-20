package com.happyhouse.challa.presentation.room.main.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface RoomMainSideEffect : UiSideEffect {
    data object ShareRequested : RoomMainSideEffect

    data object NavigateToCamera : RoomMainSideEffect

    data object NavigateToGallery : RoomMainSideEffect
}
