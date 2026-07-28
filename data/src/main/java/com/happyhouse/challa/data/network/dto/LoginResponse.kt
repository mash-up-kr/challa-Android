package com.happyhouse.challa.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val isNewUser: Boolean,
)
