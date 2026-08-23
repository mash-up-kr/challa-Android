package com.happyhouse.challa.domain.model

/**
 * 사진 한 장에 남은 반응 하나.
 *
 * @param chatId 취소할 때 쓰는 식별자
 * @param userId 남긴 사람. 사람마다 첫 반응만 사진에 스티커로 붙는다.
 * @param createdAtEpochMillis 누가 먼저 남겼는지 가리는 기준
 */
data class PhotoReaction(
    val chatId: Long,
    val userId: Long,
    val emoji: ReactionEmoji,
    val createdAtEpochMillis: Long,
)
