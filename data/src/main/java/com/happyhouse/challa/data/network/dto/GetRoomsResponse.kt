package com.happyhouse.challa.data.network.dto

import com.happyhouse.challa.domain.model.Room
import com.happyhouse.challa.domain.model.RoomStatus
import kotlinx.serialization.Serializable

@Serializable
data class GetRoomsResponse(
    val rooms: List<Room>,
) {
    @Serializable
    data class Room(
        val id: Long,
        val status: String,
        val title: String,
        val memberCount: Int,
        val totalPhotoCount: Int,
        val remainedPhotoCount: Int,
        val thumbnailImageUrls: List<String>,
        val photoPrintCompletedAt: String? = null,
    )
}

fun GetRoomsResponse.Room.toDomain(): Room =
    Room(
        id = id,
        status = RoomStatus.from(status),
        title = title,
        memberCount = memberCount,
        totalPhotoCount = totalPhotoCount,
        remainedPhotoCount = remainedPhotoCount,
        thumbnailImageUrls = thumbnailImageUrls,
        photoPrintCompletedAt = photoPrintCompletedAt,
    )
