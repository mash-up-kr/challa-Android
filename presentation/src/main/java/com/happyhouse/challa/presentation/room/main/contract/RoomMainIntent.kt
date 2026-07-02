package com.happyhouse.challa.presentation.room.main.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface RoomMainIntent : UiIntent {
    data object FetchData : RoomMainIntent
}
