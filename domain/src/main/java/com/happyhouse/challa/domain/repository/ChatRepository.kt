package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.model.chat.ChatPage
import com.happyhouse.challa.domain.result.ChallaResult

interface ChatRepository {
    /** @param page 0부터 시작한다. */
    suspend fun getChats(
        roomId: Long,
        page: Int,
    ): ChallaResult<ChatPage>
}
