package com.happyhouse.challa.presentation.home

import com.happyhouse.challa.presentation.base.UiIntent
import com.happyhouse.challa.presentation.home.model.Room

sealed interface HomeIntent : UiIntent {
    data object ClickInviteCode : HomeIntent

    data object ClickCreateRoom : HomeIntent

    data class ClickRoom(
        val room: Room,
    ) : HomeIntent
}
