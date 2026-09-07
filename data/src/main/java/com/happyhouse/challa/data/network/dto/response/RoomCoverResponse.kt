package com.happyhouse.challa.data.network.dto.response

import com.happyhouse.challa.domain.model.RoomCover
import com.happyhouse.challa.domain.model.RoomCoverColor
import com.happyhouse.challa.domain.model.RoomCoverSticker
import kotlinx.serialization.Serializable

/** 방에 적용된 커버. 방 목록·방 상세 응답이 함께 쓴다. */
@Serializable
data class RoomCoverResponse(
    val coverImageUrl: String? = null,
    val sticker: Sticker? = null,
) {
    /** 방에 적용된 스티커. 어떤 색으로 적용했는지 함께 온다. */
    @Serializable
    data class Sticker(
        val id: Long,
        val imageUrl: String,
        val color: RoomCoverColorResponse,
    )
}

@Serializable
data class RoomCoverColorResponse(
    val id: Long,
    val name: String,
    val hex: String,
)

fun RoomCoverResponse.toRoomCover(): RoomCover =
    RoomCover(
        imageUrl = coverImageUrl,
        sticker =
            sticker?.let {
                RoomCoverSticker(
                    id = it.id,
                    imageUrl = it.imageUrl,
                    color = it.color.toRoomCoverColor(),
                )
            },
    )

fun RoomCoverColorResponse.toRoomCoverColor(): RoomCoverColor =
    RoomCoverColor(
        id = id,
        hex = hex,
    )
