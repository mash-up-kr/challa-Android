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
    val nickname: String = "",
    val profileImageUrl: String? = null,
    val rooms: ImmutableList<RoomUiModel> = persistentListOf(),
    /**
     * 방 목록 API가 한 번 이상 성공적으로 조회되었는지 여부.
     *
     * 초기 빈 목록이나 조회 실패를 실제 빈 방 목록과 구분하여
     * WebSocket 구독 목록이 의도치 않게 초기화되는 것을 방지한다.
     */
    val hasLoadedRooms: Boolean = false,
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
