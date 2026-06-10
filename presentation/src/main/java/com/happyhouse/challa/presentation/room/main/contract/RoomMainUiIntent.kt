package com.happyhouse.challa.presentation.room.main.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface RoomMainUiIntent : UiIntent {
    data object FetchData : RoomMainUiIntent

    data object ShareClick : RoomMainUiIntent
}
