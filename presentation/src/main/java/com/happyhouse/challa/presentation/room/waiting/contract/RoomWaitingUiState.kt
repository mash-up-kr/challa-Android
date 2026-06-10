package com.happyhouse.challa.presentation.room.waiting.contract

import com.happyhouse.challa.presentation.base.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class RoomWaitingUiState(
    val roomId: Long = 0L,
    val memberInitials: ImmutableList<String> = persistentListOf("김", "이", "박"),
    val opensAtMillis: Long = 0L,
    val remainingSeconds: Long = 0L,
    val isInitialized: Boolean = false,
) : UiState {
    val isReadyToOpen: Boolean
        get() = isInitialized && remainingSeconds <= 0L
}
