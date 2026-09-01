package com.happyhouse.challa.domain.model

/**
 * 방 커버. 홈의 촬영 중 카드와 커버 수정 화면이 같은 그림을 그리는 데 쓴다.
 *
 * @param imageUrl 배경으로 깔 이미지. 지정하지 않았으면 null이고 화면은 검정으로 그린다.
 * @param sticker 배경 위에 올릴 스티커. 스티커를 지웠거나 아직 고르지 않았으면 null.
 */
data class RoomCover(
    val imageUrl: String? = null,
    val sticker: RoomCoverSticker? = null,
)

/**
 * 커버 스티커. 같은 스티커라도 [color]에 따라 다른 색으로 그려진다.
 *
 * @param imageUrl 스티커 이미지 주소. 앱은 여기에 [color]를 입혀 그린다.
 */
data class RoomCoverSticker(
    val id: Long,
    val imageUrl: String,
    val color: RoomCoverColor,
)

/** @param hex `#RRGGBB` 형식의 색상 문자열. */
data class RoomCoverColor(
    val id: Long,
    val name: String,
    val hex: String,
)

/** 커버 수정 화면에서 고를 수 있는 스티커·색상 목록. */
data class RoomCoverOptions(
    val stickers: List<RoomCoverSticker>,
    val colors: List<RoomCoverColor>,
)
