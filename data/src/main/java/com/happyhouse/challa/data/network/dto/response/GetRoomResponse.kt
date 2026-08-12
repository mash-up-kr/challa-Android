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
        /** 인화가 끝나는 시각. 서버 필드명은 `photoPrintCompletedAt` 이다. */
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
