package com.happyhouse.challa.presentation.home

import com.happyhouse.challa.presentation.base.UiIntent
import com.happyhouse.challa.presentation.home.model.Room

sealed interface HomeIntent : UiIntent {
    data object InviteCodeClick : HomeIntent

    data object CreateRoomClick : HomeIntent

    data class RoomClick(
        val room: Room,
    ) : HomeIntent
}