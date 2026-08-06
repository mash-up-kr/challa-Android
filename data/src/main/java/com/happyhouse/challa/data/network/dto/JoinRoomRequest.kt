package com.happyhouse.challa.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class JoinRoomRequest(
    val room: Room,
) {
    @Serializable
    data class Room(
        val invitationCode: String,
    )
}
