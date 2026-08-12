package com.happyhouse.challa.data.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class TestNotificationResponse(
    val notification: Notification,
) {
    @Serializable
    data class Notification(
        val sentCount: Int,
    )
}
