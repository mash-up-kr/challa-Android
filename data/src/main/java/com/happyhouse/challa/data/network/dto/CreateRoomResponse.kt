package com.happyhouse.challa.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateRoomResponse(
    val room: Room,
) {
    @Serializable
    data class Room(
        val id: Long,
    )
}
