package com.happyhouse.challa.presentation.room.realtime

import com.happyhouse.challa.domain.model.RoomMemberJoinedEvent

/** 닉네임·주사격 조사 영역과 방 제목 영역을 나눈 토스트 표시 모델. */
internal data class RoomMemberJoinedMessage(
    val leadingText: String,
    val roomTitle: String,
)

/** 닉네임 길이와 한글 받침 여부를 반영해 방 참여 토스트 문구를 만든다. */
internal fun RoomMemberJoinedEvent.toDisplayMessage(): RoomMemberJoinedMessage =
    RoomMemberJoinedMessage(
        leadingText = "${nickname.toDisplayNickname()}${nickname.subjectParticle()} ",
        roomTitle = roomTitle,
    )

private fun String.toDisplayNickname(): String {
    val codePointCount = codePointCount(0, length)
    if (codePointCount < NICKNAME_ELLIPSIS_THRESHOLD) return this

    val endIndex = offsetByCodePoints(0, NICKNAME_VISIBLE_CODE_POINT_COUNT)
    return substring(0, endIndex) + ELLIPSIS
}

private fun String.subjectParticle(): String = if (hasFinalConsonant()) "이" else "가"

private fun String.hasFinalConsonant(): Boolean {
    val nickname = trim()
    if (nickname.isEmpty()) return false

    val lastCodePoint = nickname.codePointBefore(nickname.length)
    return when {
        lastCodePoint in HANGUL_SYLLABLE_START..HANGUL_SYLLABLE_END ->
            (lastCodePoint - HANGUL_SYLLABLE_START) % HANGUL_FINAL_CONSONANT_COUNT != 0

        lastCodePoint in KOREAN_CONSONANT_START..KOREAN_CONSONANT_END -> true
        lastCodePoint in '0'.code..'9'.code -> lastCodePoint in DIGITS_WITH_FINAL_CONSONANT
        else -> false
    }
}

private const val NICKNAME_ELLIPSIS_THRESHOLD = 8
private const val NICKNAME_VISIBLE_CODE_POINT_COUNT = 7
private const val ELLIPSIS = "…"
private const val HANGUL_SYLLABLE_START = 0xAC00
private const val HANGUL_SYLLABLE_END = 0xD7A3
private const val HANGUL_FINAL_CONSONANT_COUNT = 28
private const val KOREAN_CONSONANT_START = 0x3131
private const val KOREAN_CONSONANT_END = 0x314E
private val DIGITS_WITH_FINAL_CONSONANT = setOf('0'.code, '1'.code, '3'.code, '6'.code, '7'.code, '8'.code)
