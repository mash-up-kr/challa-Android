package com.happyhouse.challa.data.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val provider: String,
    val idToken: String,
    val authorizationCode: String? = null,
)
