package com.happyhouse.challa.presentation.roomsetting.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface RoomSettingIntent : UiIntent {
    /** 방 이름 항목을 눌러 수정 바텀시트를 열 때 */
    data object RoomNameClick : RoomSettingIntent

    /** 수정 바텀시트를 닫을 때 */
    data object EditRoomNameSheetDismiss : RoomSettingIntent

    /**
     * 수정 바텀시트에서 변경을 눌렀을 때.
     *
     * @param roomName 앞뒤 공백이 제거된 새 방 이름.
     */
    data class RoomNameSubmit(
        val roomName: String,
    ) : RoomSettingIntent
}
