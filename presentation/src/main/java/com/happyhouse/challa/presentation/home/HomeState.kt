package com.happyhouse.challa.presentation.home

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import com.happyhouse.challa.presentation.home.model.Room
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class HomeState(
    val isLoading: Boolean = false,
    val nickname: String = "",
    val profileImageUrl: String? = null,
    val rooms: ImmutableList<Room> = persistentListOf(),
) : UiState {
    /** 촬영중이거나 촬영완료한 방이 하나도 없는 상태 */
    val isEmpty: Boolean get() = rooms.isEmpty()
}
