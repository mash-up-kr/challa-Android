package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.model.PhotoReaction
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.domain.model.chat.ChatPage
import com.happyhouse.challa.domain.model.chat.ChatSubscriptionEvent
import com.happyhouse.challa.domain.result.ChallaResult
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    /** 구독 확인과 이후 수신한 채팅을 전달하는 cold stream이다. */
    fun observeChats(roomId: Long): Flow<ChatSubscriptionEvent>

    /** @param page 0부터 시작한다. */
    suspend fun getChats(
        roomId: Long,
        page: Int,
    ): ChallaResult<ChatPage>

    suspend fun sendChat(
        roomId: Long,
        content: String,
    ): ChallaResult<Unit>

    suspend fun sendPhotoComment(
        roomId: Long,
        photoId: Long,
        message: String,
    ): ChallaResult<Unit>

    /** 사진 한 장에 남은 모든 사람의 반응. 남긴 시각 오름차순이다. */
    suspend fun getPhotoReactions(
        roomId: Long,
        photoId: Long,
    ): ChallaResult<List<PhotoReaction>>

    /** @return 남긴 반응의 chatId. 취소할 때 쓴다. */
    suspend fun addPhotoReaction(
        roomId: Long,
        photoId: Long,
        emoji: ReactionEmoji,
    ): ChallaResult<Long>

    suspend fun removePhotoReaction(chatId: Long): ChallaResult<Unit>
}
