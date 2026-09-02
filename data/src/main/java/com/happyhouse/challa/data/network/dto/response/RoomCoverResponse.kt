package com.happyhouse.challa.data.network.dto.response

import com.happyhouse.challa.domain.model.RoomCover
import com.happyhouse.challa.domain.model.RoomCoverColor
import com.happyhouse.challa.domain.model.RoomCoverOptions
import com.happyhouse.challa.domain.model.RoomCoverSticker
import com.happyhouse.challa.domain.model.RoomCoverStickerOption
import kotlinx.serialization.Serializable

/**
 * 방에 적용된 커버. 방 목록·방 상세 응답이 함께 쓴다.
 *
 */
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

/**
 * 고를 수 있는 스티커·색상 목록.
 *
 */
@Serializable
data class GetRoomCoverOptionsResponse(
    val room: Options,
) {
    @Serializable
    data class Options(
        val stickers: List<StickerOption>,
        val colors: List<RoomCoverColorResponse>,
    )

    @Serializable
    data class StickerOption(
        val id: Long,
        val imageUrl: String,
    )
}

fun RoomCoverResponse.toRoomCover(): RoomCover =
    RoomCover(
        imageUrl = coverImageUrl,
        sticker =
            sticker?.let {
                RoomCoverSticker(
                    id = it.id,
                    imageUrl = it.imageUrl,
                    color = it.color.toDomain(),
                )
            },
    )

fun GetRoomCoverOptionsResponse.Options.toRoomCoverOptions(): RoomCoverOptions =
    RoomCoverOptions(
        stickers = stickers.map { RoomCoverStickerOption(id = it.id, imageUrl = it.imageUrl) },
        colors = colors.map { it.toDomain() },
    )

private fun RoomCoverColorResponse.toDomain(): RoomCoverColor =
    RoomCoverColor(
        id = id,
        name = name,
        hex = hex,
    )
