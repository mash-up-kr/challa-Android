package com.happyhouse.challa.data.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class NotificationTokenRequest(
    val notification: Notification,
) {
    @Serializable
    data class Notification(
        val token: String,
    )
}
