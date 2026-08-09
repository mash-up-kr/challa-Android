package com.happyhouse.challa.domain.model

/**
 * 방 참여자
 *
 * @param profileImageUrl 프로필 사진을 등록하지 않은 참여자는 null
 */
data class RoomUser(
    val id: Long,
    val nickname: String,
    val profileImageUrl: String?,
)
