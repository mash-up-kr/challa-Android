package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.model.PhotoReaction
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.domain.result.ChallaResult

interface ChatRepository {
    /** 사진 한 장에 남은 모든 사람의 반응. 남긴 시각 오름차순이다. */
    suspend fun getPhotoReactions(
        roomId: Long,
        photoId: Long,
    ): ChallaResult<List<PhotoReaction>>

    /** @return 남긴 반응의 chatId. 취소할 때 쓴다. */
    suspend fun sendPhotoReaction(
        roomId: Long,
        photoId: Long,
        emoji: ReactionEmoji,
    ): ChallaResult<Long>

    suspend fun deletePhotoReaction(chatId: Long): ChallaResult<Unit>

    suspend fun sendPhotoMessage(
        roomId: Long,
        photoId: Long,
        message: String,
    ): ChallaResult<Unit>
}
