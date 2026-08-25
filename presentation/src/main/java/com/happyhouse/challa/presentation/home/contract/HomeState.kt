package com.happyhouse.challa.presentation.home.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import com.happyhouse.challa.presentation.home.model.RoomUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class HomeState(
    val isLoading: Boolean = false,
    val hasLoadedRooms: Boolean = false,
    val nickname: String = "",
    val profileImageUrl: String? = null,
    val rooms: ImmutableList<RoomUiModel> = persistentListOf(),
) : UiState {
    /** 촬영중이거나 촬영완료한 방이 하나도 없는 상태 */
    val isEmpty: Boolean
        get() = rooms.isEmpty()

    /** 촬영 중인 방 목록 */
    val shootingRooms: ImmutableList<RoomUiModel.Shooting>
        get() = rooms.filterIsInstance<RoomUiModel.Shooting>().toImmutableList()

    /** 촬영 완료된 방 목록 */
    val completedRooms: ImmutableList<RoomUiModel.Completed>
        get() = rooms.filterIsInstance<RoomUiModel.Completed>().toImmutableList()
}
