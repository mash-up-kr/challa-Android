package com.happyhouse.challa.data.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val user: User,
) {
    @Serializable
    data class User(
        val nickname: String,
        val profileImageUrl: String?,
    )
}
