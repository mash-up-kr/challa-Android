package com.happyhouse.challa.data.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class CameraFilterResponse(
    val shoot: Shoot,
) {
    @Serializable
    data class Shoot(
        val cameraFilters: List<CameraFilter>,
    )

    @Serializable
    data class CameraFilter(
        val name: String,
        val fileUrl: String,
    )
}
