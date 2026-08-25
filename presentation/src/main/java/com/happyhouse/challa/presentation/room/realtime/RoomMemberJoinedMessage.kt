package com.happyhouse.challa.presentation.room.realtime

import com.happyhouse.challa.domain.model.RoomMemberJoinedEvent

/** 닉네임 길이를 반영해 방 참여 토스트 문구를 만든다. */
internal fun RoomMemberJoinedEvent.toDisplayMessage(suffix: String): String = "${nickname.toDisplayNickname()}님이 $roomTitle$suffix"

private fun String.toDisplayNickname(): String {
    val codePointCount = codePointCount(0, length)
    if (codePointCount < NICKNAME_ELLIPSIS_THRESHOLD) return this

    val endIndex = offsetByCodePoints(0, NICKNAME_VISIBLE_CODE_POINT_COUNT)
    return substring(0, endIndex) + ELLIPSIS
}

private const val NICKNAME_ELLIPSIS_THRESHOLD = 8
private const val NICKNAME_VISIBLE_CODE_POINT_COUNT = 7
private const val ELLIPSIS = "…"
