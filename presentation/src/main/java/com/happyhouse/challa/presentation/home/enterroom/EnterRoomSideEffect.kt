package com.happyhouse.challa.presentation.home.enterroom

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface EnterRoomSideEffect : UiSideEffect {
    data class RoomEntered(
        val roomId: Long,
    ) : EnterRoomSideEffect

    data class RoomEnterFailed(
        val message: String?,
    ) : EnterRoomSideEffect
}
