package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.network.api.ChatApi
import com.happyhouse.challa.data.network.api.ChatWebSocketApi
import com.happyhouse.challa.data.network.api.ChatWebSocketEvent
import com.happyhouse.challa.data.network.dto.request.SendChatRequest
import com.happyhouse.challa.data.network.dto.response.ChatsResponse
import com.happyhouse.challa.data.network.parseServerInstant
import com.happyhouse.challa.domain.model.chat.Chat
import com.happyhouse.challa.domain.model.chat.ChatPage
import com.happyhouse.challa.domain.model.chat.ChatSubscriptionEvent
import com.happyhouse.challa.domain.model.chat.ChatType
import com.happyhouse.challa.domain.repository.ChatRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatApi: ChatApi,
    private val chatWebSocketApi: ChatWebSocketApi,
) : ChatRepository {
    override fun observeChats(roomId: Long): Flow<ChatSubscriptionEvent> =
        chatWebSocketApi.observeChats(roomId).map { event ->
            when (event) {
                ChatWebSocketEvent.Subscribed -> ChatSubscriptionEvent.Subscribed
                is ChatWebSocketEvent.ChatsReceived ->
                    ChatSubscriptionEvent.ChatsReceived(event.chats.map(ChatsResponse.ChatItem::toChat))
            }
        }

    override suspend fun sendChat(
        roomId: Long,
        content: String,
    ): ChallaResult<Unit> =
        chatApi
            .sendChat(
                SendChatRequest(
                    chat =
                        SendChatRequest.Chat(
                            roomId = roomId,
                            photoId = TEXT_CHAT_PHOTO_ID,
                            type = SendChatRequest.Chat.Type.DEFAULT,
                            content = content,
                        ),
                ),
            ).mapCatching { response ->
                check(response.success) { response.message }
            }

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
        private const val TEXT_CHAT_PHOTO_ID = 0L
    }
}

private fun ChatsResponse.ChatItem.toChat(): Chat =
    Chat(
        id = chatId,
        userId = userId,
        type = ChatType.valueOf(type.name),
        content = content,
        photoId = photoId,
        photoImageUrl = photoImageUrl,
        createdAt = createdAt.parseServerInstant(),
        userName = userName,
        userProfileImageUrl = userProfileImageUrl,
    )
