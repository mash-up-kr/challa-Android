package com.happyhouse.challa.data.network.dto.response

import com.happyhouse.challa.domain.model.RoomCoverOptions
import com.happyhouse.challa.domain.model.RoomCoverStickerOption
import kotlinx.serialization.Serializable

/**
 * 고를 수 있는 스티커·색상 목록.
 *
 * 스웨거는 여기 스티커에도 `color`가 있다고 하지만 실제 응답에는 없다(2026-09-02 확인).
 * 색은 `colors`에서 따로 고르는 구조라 스티커에 색을 요구하면 파싱이 통째로 실패한다.
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

fun GetRoomCoverOptionsResponse.Options.toRoomCoverOptions(): RoomCoverOptions =
    RoomCoverOptions(
        stickers = stickers.map { RoomCoverStickerOption(id = it.id, imageUrl = it.imageUrl) },
        colors = colors.map { it.toRoomCoverColor() },
    )
