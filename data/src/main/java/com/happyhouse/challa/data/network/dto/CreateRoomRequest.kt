package com.happyhouse.challa.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateRoomRequest(
    val room: Room,
) {
    @Serializable
    data class Room(
        val title: String,
        val totalPhotoCount: Int,
    )
}
