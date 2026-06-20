package com.happyhouse.challa.presentation.room.main.contract

import com.happyhouse.challa.presentation.base.UiState
import com.happyhouse.challa.presentation.model.Room
import com.happyhouse.challa.presentation.model.RoomStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Duration.Companion.hours

data class RoomMainState(
    val room: Room =
        Room(
            id = "room-id",
            name = "해피하우스 프작모",
            status = RoomStatus.Shooting(taken = 11, total = 24),
        ),
    val memberInitials: ImmutableList<String> = persistentListOf("박", "김", "이"),
    val maxMemberCount: Int = 12,
) : UiState {
    val title: String
        get() = room.name

    val status: RoomStatus
        get() = room.status

    val photoCount: Int
        get() = (status as? RoomStatus.Shooting)?.taken ?: 24

    val totalPhotoCount: Int
        get() = (status as? RoomStatus.Shooting)?.total ?: 24

    companion object {
        fun waiting(): RoomMainState =
            RoomMainState(
                room =
                    Room(
                        id = "room-id",
                        name = "해피하우스 프작모",
                        status = RoomStatus.Waiting(dDay = 0, remaining = 3.hours),
                    ),
            )

        fun opened(): RoomMainState =
            RoomMainState(
                room =
                    Room(
                        id = "room-id",
                        name = "해피하우스 프작모",
                        status = RoomStatus.Opened,
                    ),
            )
    }
}
