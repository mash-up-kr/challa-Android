package com.happyhouse.challa.presentation.room.waiting.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface RoomWaitingUiIntent : UiIntent {
    data class Initialize(
        val roomId: Long,
        val opensAtMillis: Long,
    ) : RoomWaitingUiIntent

    data object ClickBack : RoomWaitingUiIntent

    data object ClickShare : RoomWaitingUiIntent

    data object ClickOpen : RoomWaitingUiIntent
}
