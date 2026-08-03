package com.happyhouse.challa.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val auth: Auth,
) {
    @Serializable
    data class Auth(
        val provider: String,
        val idToken: String,
        val authorizationCode: String? = null,
    )
}
