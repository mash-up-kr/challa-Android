package com.happyhouse.challa.data.network.dto.response

import com.happyhouse.challa.domain.model.RoomCover
import com.happyhouse.challa.domain.model.RoomCoverColor
import com.happyhouse.challa.domain.model.RoomCoverOptions
import com.happyhouse.challa.domain.model.RoomCoverSticker
import kotlinx.serialization.Serializable

/**
 * 방 커버. 방 목록·방 상세 응답이 함께 쓴다.
 *
 * 스웨거에 `required`가 없어 두 필드 모두 응답에서 빠질 수 있다. 기본값이 없으면
 * `MissingFieldException`으로 방 조회가 통째로 실패하므로 반드시 남겨둔다.
 */
@Serializable
data class RoomCoverResponse(
    val coverImageUrl: String? = null,
    val sticker: Sticker? = null,
) {
    @Serializable
    data class Sticker(
        val id: Long,
        val imageUrl: String,
        val color: Color,
    )

    @Serializable
    data class Color(
        val id: Long,
        val name: String,
        val hex: String,
    )
}

@Serializable
data class GetRoomCoverOptionsResponse(
    val room: Options,
) {
    @Serializable
    data class Options(
        val stickers: List<RoomCoverResponse.Sticker>,
        val colors: List<RoomCoverResponse.Color>,
    )
}

fun RoomCoverResponse.toRoomCover(): RoomCover =
    RoomCover(
        imageUrl = coverImageUrl,
        sticker = sticker?.toDomain(),
    )

fun GetRoomCoverOptionsResponse.Options.toRoomCoverOptions(): RoomCoverOptions =
    RoomCoverOptions(
        stickers = stickers.map { it.toDomain() },
        colors = colors.map { it.toDomain() },
    )

private fun RoomCoverResponse.Sticker.toDomain(): RoomCoverSticker =
    RoomCoverSticker(
        id = id,
        imageUrl = imageUrl,
        color = color.toDomain(),
    )

private fun RoomCoverResponse.Color.toDomain(): RoomCoverColor =
    RoomCoverColor(
        id = id,
        name = name,
        hex = hex,
    )
