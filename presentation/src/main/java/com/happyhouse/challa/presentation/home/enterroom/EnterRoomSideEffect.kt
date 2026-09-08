package com.happyhouse.challa.presentation.home.enterroom

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface EnterRoomSideEffect : UiSideEffect {
    data class RoomEntered(
        val roomId: Long,
    ) : EnterRoomSideEffect

    data object RoomEnterFailed : EnterRoomSideEffect
}
