package com.happyhouse.challa.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreatePhotoRequest(
    val photo: Photo,
) {
    @Serializable
    data class Photo(
        val roomId: Long,
        val cameraFilterName: String,
        val imageUrl: String,
    )
}
