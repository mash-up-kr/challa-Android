package com.happyhouse.challa.presentation.model

import androidx.compose.runtime.Immutable
import kotlin.time.Duration

@Immutable
sealed interface RoomStatus {
    data class Shooting(
        val taken: Int,
    ) : RoomStatus

    data class Waiting(
        val dDay: Int,
        val remaining: Duration,
    ) : RoomStatus

    data object Opened : RoomStatus

    data class Expiring(
        val dDay: Int,
    ) : RoomStatus
}
