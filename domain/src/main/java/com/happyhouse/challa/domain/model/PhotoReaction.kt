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

/**
 * 사진에 스티커로 붙일 반응만 추린다.
 *
 * 사람마다 **가장 먼저 남긴 반응 하나**만 남기고, 먼저 남긴 순으로 [limit]명까지 자른다.
 * 나머지는 채팅 기록에만 쌓인다.
 *
 * 첫 반응을 취소하면 그 사람의 다음 반응이 자동으로 스티커가 된다.
 */
fun List<PhotoReaction>.toStickerReactions(limit: Int): List<PhotoReaction> =
    sortedBy { reaction -> reaction.createdAtEpochMillis }
        .distinctBy { reaction -> reaction.userId }
        .take(limit)
