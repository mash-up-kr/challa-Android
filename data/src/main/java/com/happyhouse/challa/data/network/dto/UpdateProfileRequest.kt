package com.happyhouse.challa.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val nickname: String,
    val profileImageUrl: String?,
)
