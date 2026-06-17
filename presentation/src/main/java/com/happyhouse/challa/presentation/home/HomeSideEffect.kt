package com.happyhouse.challa.presentation.home

import com.happyhouse.challa.presentation.base.UiSideEffect
import com.happyhouse.challa.presentation.home.model.Room

sealed interface HomeSideEffect : UiSideEffect {
    data object InviteCodeEntryRequested : HomeSideEffect

    data object RoomCreationRequested : HomeSideEffect

    data class RoomSelected(
        val room: Room,
    ) : HomeSideEffect
}
