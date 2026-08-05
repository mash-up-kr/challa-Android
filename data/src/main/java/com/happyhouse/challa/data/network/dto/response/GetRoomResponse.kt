package com.happyhouse.challa.data.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class GetRoomResponse(
    val room: Room,
) {
    @Serializable
    data class Room(
        val id: Long? = null,
        val title: String,
        val totalPhotoCount: Int,
        val remainedPhotoCount: Int,
        val invitationCode: String,
        val status: String,
        val photoPrintCompletionAt: String? = null,
    )
}
