package com.happyhouse.challa.data.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class CameraFilterResponse(
    val cameraFilters: List<CameraFilter>,
) {
    @Serializable
    data class CameraFilter(
        val id: Long,
        val name: String,
        val fileUrl: String,
    )
}
