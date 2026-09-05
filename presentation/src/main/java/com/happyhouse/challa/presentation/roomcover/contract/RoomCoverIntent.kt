package com.happyhouse.challa.presentation.roomcover.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface RoomCoverIntent : UiIntent {
    /** 팔레트에서 색을 골랐을 때 */
    data class ColorClick(
        val color: RoomCoverColorUiModel,
    ) : RoomCoverIntent

    /** 스티커를 골랐을 때. 이미 고른 스티커를 다시 누르면 해제한다. */
    data class StickerClick(
        val sticker: RoomCoverStickerUiModel,
    ) : RoomCoverIntent

    /**
     * 갤러리에서 배경으로 쓸 사진을 골랐을 때.
     *
     * @param imageUri 고른 사진의 content URI 문자열.
     */
    data class BackgroundImageSelect(
        val imageUri: String,
    ) : RoomCoverIntent

    /** 배경 이미지를 지울 때. 스티커와 색은 그대로 둔다. */
    data object BackgroundImageRemoveClick : RoomCoverIntent

    /** 불러오기에 실패한 뒤 다시 시도할 때 */
    data object RetryClick : RoomCoverIntent
}
