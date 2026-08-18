package com.happyhouse.challa.data.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
internal data class RoomMemberJoinedResponse(
    val room: Room,
) {
    @Serializable
    data class Room(
        val id: Long,
        val title: String,
        val userNickname: String,
        val userProfileImageUrl: String? = null,
    )
}
