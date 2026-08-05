package com.happyhouse.challa.presentation.home.createroom

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface CreateRoomSideEffect : UiSideEffect {
    data class RoomCreated(
        val roomId: Long,
        val roomName: String,
    ) : CreateRoomSideEffect

    data class RoomCreateFailed(
        val message: String?,
    ) : CreateRoomSideEffect
}
