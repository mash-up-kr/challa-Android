package com.happyhouse.challa.presentation.room.main.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import com.happyhouse.challa.presentation.model.Room
import com.happyhouse.challa.presentation.model.RoomStatus
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface RoomMainState : UiState {
    data object Loading : RoomMainState

    data class Content(
        val room: Room,
        val memberInitials: ImmutableList<String>,
        val maxMemberCount: Int,
    ) : RoomMainState {
        val title: String
            get() = room.name

        val photoCount: Int
            get() =
                when (room.status) {
                    is RoomStatus.Shooting -> room.status.taken
                    is RoomStatus.Waiting,
                    RoomStatus.Opened,
                    is RoomStatus.Expiring,
                    -> room.requiredPhotoCount
                }

        val totalPhotoCount: Int
            get() = room.requiredPhotoCount
    }

    data object Error : RoomMainState
}
