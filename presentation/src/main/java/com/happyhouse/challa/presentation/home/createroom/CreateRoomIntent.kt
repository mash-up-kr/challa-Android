package com.happyhouse.challa.presentation.home.createroom

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface CreateRoomIntent : UiIntent {
    data class NameChanged(
        val name: String,
    ) : CreateRoomIntent

    data object CreateClick : CreateRoomIntent
}
