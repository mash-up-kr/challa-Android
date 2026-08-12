package com.happyhouse.challa.data.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ListPhotosResponse(
    val photo: List<Photo>,
) {
    @Serializable
    data class Photo(
        val id: Long,
        val imageUrl: String,
    )
}
