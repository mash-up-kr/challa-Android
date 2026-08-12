package com.happyhouse.challa.data.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class TestNotificationRequest(
    val notification: Notification,
) {
    @Serializable
    data class Notification(
        val title: String? = null,
        val body: String? = null,
    )
}
