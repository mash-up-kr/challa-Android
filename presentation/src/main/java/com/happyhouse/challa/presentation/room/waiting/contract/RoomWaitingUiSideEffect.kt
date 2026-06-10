package com.happyhouse.challa.presentation.room.waiting.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface RoomWaitingUiSideEffect : UiSideEffect {
    data class ShareLink(
        val link: String,
        val title: String,
        val description: String,
    ) : RoomWaitingUiSideEffect

    data class NavigateToRoom(
        val roomId: Long,
    ) : RoomWaitingUiSideEffect
}
