package com.happyhouse.challa.presentation.invite

/** 초대 링크로 요청한 방 입장의 결과. */
sealed interface RoomInviteEvent {
    data class RoomEntered(
        val roomId: Long,
    ) : RoomInviteEvent

    data class RoomEnterFailed(
        val message: String?,
    ) : RoomInviteEvent
}
