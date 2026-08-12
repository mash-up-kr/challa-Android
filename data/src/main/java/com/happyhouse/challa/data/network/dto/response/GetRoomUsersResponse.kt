package com.happyhouse.challa.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * 방 참여자 목록 응답
 *
 * 서버가 단수형 `room` 키에 참여자 배열을 담아 내려준다. 사진 목록의 `photo` 와 같은 형태다.
 */
@Serializable
data class GetRoomUsersResponse(
    val room: List<User>,
) {
    @Serializable
    data class User(
        val id: Long,
        val nickname: String,
        val profileImageUrl: String? = null,
    )
}
