package com.happyhouse.challa.domain.repository

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
}
