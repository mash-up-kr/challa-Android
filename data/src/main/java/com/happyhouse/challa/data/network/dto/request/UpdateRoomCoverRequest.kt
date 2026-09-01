package com.happyhouse.challa.data.network.dto.request

import kotlinx.serialization.Serializable

/**
 * 방 커버 저장 요청. 부분 갱신이 아니라 전체 교체로 보고 세 필드를 항상 함께 보낸다.
 * 값이 null이면 해당 항목을 지운다는 뜻이다.
 */
@Serializable
data class UpdateRoomCoverRequest(
    val room: Room,
) {
    @Serializable
    data class Room(
        val coverImageUrl: String?,
        val coverStickerId: Long?,
        val coverStickerColorId: Long?,
    )
}
