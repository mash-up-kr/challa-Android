package com.happyhouse.challa.presentation.home.createroom

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface CreateRoomIntent : UiIntent {
    data class NameChanged(
        val name: String,
    ) : CreateRoomIntent

    data class ShotCountChanged(
        val shotCount: ShotCount,
    ) : CreateRoomIntent

    data object CreateClick : CreateRoomIntent

    /** 바텀시트를 다시 열 때 이전 입력이 남지 않도록 폼 상태를 초기화한다. */
    data object Reset : CreateRoomIntent
}
