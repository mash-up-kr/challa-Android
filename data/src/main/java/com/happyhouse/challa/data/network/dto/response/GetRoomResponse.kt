package com.happyhouse.challa.data.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class GetRoomResponse(
    val room: Room,
) {
    @Serializable
    data class Room(
        val id: Long,
        val title: String,
        val totalPhotoCount: Int,
        val remainedPhotoCount: Int,
        val invitationCode: String,
        val status: Status = Status.UNKNOWN,
        // 커버를 배정받지 못한 예전 방은 이 필드가 빠져 올 수 있다.
        val cover: RoomCoverResponse = RoomCoverResponse(),
        val photoPrintCompletedAt: String? = null,
    )

    /**
     * 방 상태
     */
    @Serializable
    enum class Status {
        SHOOTING,
        PHOTO_PRINT_PENDING,
        PHOTO_PRINT_COMPLETED,
        UNKNOWN,
    }
}
