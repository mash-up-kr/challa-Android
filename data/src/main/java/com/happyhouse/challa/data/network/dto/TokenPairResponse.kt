package com.happyhouse.challa.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class TokenPairResponse(
    val accessToken: String,
    val refreshToken: String,
)
