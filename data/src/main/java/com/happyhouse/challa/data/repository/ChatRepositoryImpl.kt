package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.network.api.ChatApi
import com.happyhouse.challa.data.network.dto.response.ChatsResponse
import com.happyhouse.challa.data.network.parseServerInstant
import com.happyhouse.challa.domain.model.chat.Chat
import com.happyhouse.challa.domain.model.chat.ChatPage
import com.happyhouse.challa.domain.model.chat.ChatType
import com.happyhouse.challa.domain.repository.ChatRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatApi: ChatApi,
) : ChatRepository {
    override suspend fun getChats(
        roomId: Long,
        page: Int,
    ): ChallaResult<ChatPage> =
        chatApi
            .getChats(roomId = roomId, page = page, size = CHAT_PAGE_SIZE)
            .mapCatching { response ->
                check(response.success) { response.message }
                val data = requireNotNull(response.data) { "채팅 목록 응답 데이터가 비어 있습니다." }

                ChatPage(
                    chats = data.chats.map { it.toChat() },
                    hasNext = data.chats.size == CHAT_PAGE_SIZE,
                )
            }

    companion object {
        private const val CHAT_PAGE_SIZE = 20
    }
}

private fun ChatsResponse.ChatItem.toChat(): Chat =
    Chat(
        userId = userId,
        type = ChatType.valueOf(type.name),
        content = content,
        photoId = photoId,
        photoImageUrl = photoImageUrl,
        createdAt = createdAt.parseServerInstant(),
        userName = userName,
        userProfileImageUrl = userProfileImageUrl,
    )
