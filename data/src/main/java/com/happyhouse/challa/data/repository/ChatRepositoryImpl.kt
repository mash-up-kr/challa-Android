package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.network.api.ChatApi
import com.happyhouse.challa.data.network.api.ChatWebSocketApi
import com.happyhouse.challa.data.network.api.ChatWebSocketEvent
import com.happyhouse.challa.data.network.api.PhotoApi
import com.happyhouse.challa.data.network.dto.request.CreateChatRequest
import com.happyhouse.challa.data.network.dto.request.SendChatRequest
import com.happyhouse.challa.data.network.dto.response.toChat
import com.happyhouse.challa.data.network.dto.response.toPhotoReactions
import com.happyhouse.challa.domain.model.PhotoReaction
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.domain.model.chat.ChatPage
import com.happyhouse.challa.domain.model.chat.ChatSubscriptionEvent
import com.happyhouse.challa.domain.repository.ChatRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatApi: ChatApi,
    // 반응·댓글은 사진 상세 응답에 실려 오므로 photos 엔드포인트로 받는다.
    private val photoApi: PhotoApi,
    private val chatWebSocketApi: ChatWebSocketApi,
) : ChatRepository {
    override fun observeChats(roomId: Long): Flow<ChatSubscriptionEvent> =
        chatWebSocketApi.observeChats(roomId).map { event ->
            when (event) {
                ChatWebSocketEvent.Subscribed -> ChatSubscriptionEvent.Subscribed
                is ChatWebSocketEvent.ChatReceived ->
                    ChatSubscriptionEvent.ChatReceived(event.chat.toChat())
            }
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

    override suspend fun sendChat(
        roomId: Long,
        content: String,
    ): ChallaResult<Unit> =
        chatApi
            .postChat(
                SendChatRequest(
                    chat =
                        SendChatRequest.Chat(
                            roomId = roomId,
                            type = SendChatRequest.Chat.Type.DEFAULT,
                            content = content,
                        ),
                ),
            ).mapCatching { response ->
                check(response.success) { response.message }
            }

    override suspend fun sendPhotoComment(
        roomId: Long,
        photoId: Long,
        message: String,
    ): ChallaResult<Unit> =
        postPhotoChat(
            roomId = roomId,
            photoId = photoId,
            type = CreateChatRequest.ChatType.COMMENT,
            content = message,
        ).mapCatching { }

    override suspend fun getPhotoReactions(
        roomId: Long,
        photoId: Long,
    ): ChallaResult<List<PhotoReaction>> =
        photoApi
            .getPhotoDetail(photoId = photoId, roomId = roomId)
            .mapCatching { response ->
                check(response.success) { response.message }
                val data = requireNotNull(response.data) { "사진 상세 응답 데이터가 비어 있습니다." }
                data.toPhotoReactions()
            }

    override suspend fun addPhotoReaction(
        roomId: Long,
        photoId: Long,
        emoji: ReactionEmoji,
    ): ChallaResult<Long> =
        postPhotoChat(
            roomId = roomId,
            photoId = photoId,
            type = CreateChatRequest.ChatType.EMOJI,
            content = emoji.name,
        ).mapCatching { response -> response.chat.chatId }

    override suspend fun removePhotoReaction(chatId: Long): ChallaResult<Unit> =
        chatApi
            .deletePhotoReaction(chatId)
            .mapCatching { response -> check(response.success) { response.message } }

    private suspend fun postPhotoChat(
        roomId: Long,
        photoId: Long,
        type: CreateChatRequest.ChatType,
        content: String,
    ) = chatApi
        .postPhotoChat(
            CreateChatRequest(
                chat =
                    CreateChatRequest.Chat(
                        roomId = roomId,
                        photoId = photoId,
                        type = type,
                        content = content,
                    ),
            ),
        ).mapCatching { response ->
            check(response.success) { response.message }
            requireNotNull(response.data) { "반응 등록 응답 데이터가 비어 있습니다." }
        }

    companion object {
        private const val CHAT_PAGE_SIZE = 20
    }
}
