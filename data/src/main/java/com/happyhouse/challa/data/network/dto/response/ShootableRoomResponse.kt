package com.happyhouse.challa.data.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ShootableRoomResponse(
    val rooms: List<Room>,
) {
    @Serializable
    data class Room(
        val id: Long,
        val title: String,
        val remainedPhotoCount: Int,
        val totalPhotoCount: Int,
    )
}
