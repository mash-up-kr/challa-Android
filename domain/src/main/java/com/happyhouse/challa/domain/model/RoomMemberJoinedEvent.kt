package com.happyhouse.challa.domain.model

data class RoomMemberJoinedEvent(
    val roomId: Long,
    val roomTitle: String,
    val nickname: String,
    val userProfileImageUrl: String?,
)
