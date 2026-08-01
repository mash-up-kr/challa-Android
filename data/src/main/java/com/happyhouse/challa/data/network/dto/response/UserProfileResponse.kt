package com.happyhouse.challa.data.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    val id: Long,
    val nickname: String? = null,
    val profileImageUrl: String? = null,
)
