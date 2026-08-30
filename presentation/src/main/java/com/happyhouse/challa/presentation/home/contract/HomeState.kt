package com.happyhouse.challa.presentation.home.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import com.happyhouse.challa.presentation.home.model.RoomUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class HomeState(
    val roomLoadState: HomeRoomLoadState = HomeRoomLoadState.LOADING,
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

    /** 인화 전(인화 대기) 방 목록 */
    val printingRooms: ImmutableList<RoomUiModel.Printing>
        get() = rooms.filterIsInstance<RoomUiModel.Printing>().toImmutableList()

    /** 인화 완료된 방 목록 */
    val completedRooms: ImmutableList<RoomUiModel.Completed>
        get() = rooms.filterIsInstance<RoomUiModel.Completed>().toImmutableList()
}

enum class HomeRoomLoadState {
    /** 첫 조회 중. 아직 그릴 목록이 없어 화면 전체를 로딩으로 덮는다. */
    LOADING,

    /** 이미 그려둔 홈 위에서 목록을 다시 받는 중 */
    REFRESHING,
    LOADED,
    FAILED,
}
