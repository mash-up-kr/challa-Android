package com.happyhouse.challa.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoReactionTest {
    private fun reaction(
        chatId: Long,
        userId: Long,
        emoji: ReactionEmoji,
        at: Long,
    ) = PhotoReaction(chatId = chatId, userId = userId, emoji = emoji, createdAtEpochMillis = at)

    @Test
    fun `사람마다 가장 먼저 남긴 반응만 스티커가 된다`() {
        val reactions =
            listOf(
                reaction(chatId = 1, userId = 10, emoji = ReactionEmoji.FIRE, at = 100),
                reaction(chatId = 2, userId = 10, emoji = ReactionEmoji.EYES, at = 200),
                reaction(chatId = 3, userId = 20, emoji = ReactionEmoji.HEART, at = 300),
            )

        val stickers = reactions.toStickerReactions(limit = 3)

        assertEquals(listOf(1L, 3L), stickers.map { it.chatId })
    }

    @Test
    fun `첫 반응을 취소하면 그 사람의 다음 반응이 스티커가 된다`() {
        val afterCancel =
            listOf(
                // chatId=1(FIRE)을 취소해 목록에서 빠진 상태
                reaction(chatId = 2, userId = 10, emoji = ReactionEmoji.EYES, at = 200),
                reaction(chatId = 3, userId = 10, emoji = ReactionEmoji.QUESTION, at = 300),
            )

        val stickers = afterCancel.toStickerReactions(limit = 3)

        assertEquals(listOf(ReactionEmoji.EYES), stickers.map { it.emoji })
    }

    @Test
    fun `먼저 남긴 순으로 정해진 사람 수까지만 붙는다`() {
        val reactions =
            (1L..5L).map { userId ->
                reaction(chatId = userId, userId = userId, emoji = ReactionEmoji.FIRE, at = userId * 100)
            }

        val stickers = reactions.toStickerReactions(limit = 3)

        assertEquals(listOf(1L, 2L, 3L), stickers.map { it.userId })
    }

    @Test
    fun `순서가 뒤섞여 들어와도 남긴 시각 기준으로 정한다`() {
        val reactions =
            listOf(
                reaction(chatId = 3, userId = 10, emoji = ReactionEmoji.QUESTION, at = 300),
                reaction(chatId = 1, userId = 10, emoji = ReactionEmoji.FIRE, at = 100),
            )

        val stickers = reactions.toStickerReactions(limit = 3)

        assertEquals(listOf(ReactionEmoji.FIRE), stickers.map { it.emoji })
    }
}
