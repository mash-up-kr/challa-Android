package com.happyhouse.challa.presentation.home.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface HomeSideEffect : UiSideEffect {
    data class RoomsLoaded(
        val roomIds: Set<Long>,
    ) : HomeSideEffect

    data object RoomsLoadFailed : HomeSideEffect
}
