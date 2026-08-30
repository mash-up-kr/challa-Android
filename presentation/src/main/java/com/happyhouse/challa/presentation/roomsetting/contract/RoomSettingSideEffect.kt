package com.happyhouse.challa.presentation.roomsetting.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface RoomSettingSideEffect : UiSideEffect {
    /** 방 이름을 저장하지 못했을 때. 이전 이름을 그대로 두고 알리기만 한다. */
    data object RoomNameUpdateFailed : RoomSettingSideEffect
}
