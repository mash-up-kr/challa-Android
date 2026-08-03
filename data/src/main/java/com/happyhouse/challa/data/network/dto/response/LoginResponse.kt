package com.happyhouse.challa.data.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val auth: Auth,
) {
    @Serializable
    data class Auth(
        val accessToken: String,
        val refreshToken: String,
        val isNew: Boolean,
    )
}
