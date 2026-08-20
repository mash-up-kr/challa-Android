package com.happyhouse.challa.data.network.dto.response

import kotlinx.serialization.Serializable

/** STOMP `MESSAGE` body의 `data`에 포함되는 방 참여 이벤트 payload. */
@Serializable
internal data class RoomMemberJoinedResponse(
    val room: Room,
) {
    /** 참여가 발생한 방 정보와 새로 참여한 사용자 정보를 함께 전달한다. */
    @Serializable
    data class Room(
        val id: Long,
        val title: String,
        val userNickname: String,
        val userProfileImageUrl: String? = null,
    )
}
