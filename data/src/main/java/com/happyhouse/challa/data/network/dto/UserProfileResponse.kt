package com.happyhouse.challa.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    val user: User,
) {
    @Serializable
    data class User(
        val id: Long,
        val nickname: String? = null,
        val profileImageUrl: String? = null,
    )
}
