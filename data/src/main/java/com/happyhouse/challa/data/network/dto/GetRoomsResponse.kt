package com.happyhouse.challa.data.network.dto

import com.happyhouse.challa.data.network.dto.response.RoomCoverResponse
import com.happyhouse.challa.data.network.dto.response.toRoomCover
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
        // 커버를 배정받지 못한 예전 방은 이 필드가 빠져 올 수 있다.
        val cover: RoomCoverResponse = RoomCoverResponse(),
        val photoPrintCompletedAt: String? = null,
        val photoPrintCompletionCheckedAt: String? = null,
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
        cover = cover.toRoomCover(),
        photoPrintCompletedAt = photoPrintCompletedAt,
        photoPrintCompletionCheckedAt = photoPrintCompletionCheckedAt,
    )
