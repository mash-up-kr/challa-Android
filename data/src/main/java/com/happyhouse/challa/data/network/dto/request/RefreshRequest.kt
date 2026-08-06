package com.happyhouse.challa.data.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequest(
    val auth: Auth,
) {
    @Serializable
    data class Auth(
        val refreshToken: String,
    )
}
