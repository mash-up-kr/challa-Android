package com.happyhouse.challa.domain.model

/**
 * 사진 한 장에 남은 반응 하나.
 *
 * @param chatId 취소할 때 쓰는 식별자
 */
data class PhotoReaction(
    val chatId: Long,
    val userId: Long,
    val emoji: ReactionEmoji,
    val createdAtEpochMillis: Long,
)

/**
 * 사진에 스티커로 붙일 반응만 추린다.
 *
 * 사람마다 **가장 먼저 남긴 반응 하나**만 남기고, 먼저 남긴 순으로 [limit]명까지 자른다.
 * 나머지는 채팅 기록에만 쌓인다.
 */
fun List<PhotoReaction>.toStickerReactions(limit: Int): List<PhotoReaction> =
    sortedBy { reaction -> reaction.createdAtEpochMillis }
        .distinctBy { reaction -> reaction.userId }
        .take(limit)
