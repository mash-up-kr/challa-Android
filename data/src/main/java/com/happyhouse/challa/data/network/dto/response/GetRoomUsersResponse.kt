package com.happyhouse.challa.data.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class GetRoomUsersResponse(
    val users: List<User>,
) {
    @Serializable
    data class User(
        val id: Long,
        val nickname: String,
        val profileImageUrl: String? = null,
    )
}
