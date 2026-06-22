package com.happyhouse.challa.presentation.home.createroom

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface CreateRoomSideEffect : UiSideEffect {
    data class RoomCreated(
        val roomId: String,
        val roomName: String,
    ) : CreateRoomSideEffect
}
