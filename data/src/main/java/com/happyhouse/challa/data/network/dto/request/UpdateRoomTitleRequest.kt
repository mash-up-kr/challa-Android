package com.happyhouse.challa.data.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateRoomTitleRequest(
    val room: Room,
) {
    @Serializable
    data class Room(
        val title: String,
    )
}
