package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.network.api.ChatApi
import com.happyhouse.challa.data.network.api.PhotoApi
import com.happyhouse.challa.data.network.dto.request.CreateChatRequest
import com.happyhouse.challa.data.network.dto.response.toPhotoReactions
import com.happyhouse.challa.domain.model.PhotoReaction
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.domain.repository.ChatRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatApi: ChatApi,
    // 반응·댓글은 사진 상세 응답에 실려 오므로 photos 엔드포인트로 받는다.
    private val photoApi: PhotoApi,
) : ChatRepository {
    override suspend fun getPhotoReactions(photoId: Long): ChallaResult<List<PhotoReaction>> =
        photoApi
            .getPhotoDetail(photoId)
            .mapCatching { response ->
                check(response.success) { response.message }
                val data = requireNotNull(response.data) { "사진 상세 응답 데이터가 비어 있습니다." }
                data.toPhotoReactions()
            }

    override suspend fun sendPhotoReaction(
        roomId: Long,
        photoId: Long,
        emoji: ReactionEmoji,
    ): ChallaResult<Long> =
        postReaction(
            roomId = roomId,
            photoId = photoId,
            type = CreateChatRequest.ChatType.EMOJI,
            content = emoji.name,
        ).mapCatching { response -> response.chat.chatId }

    override suspend fun deletePhotoReaction(chatId: Long): ChallaResult<Unit> =
        chatApi
            .deleteChatForReaction(chatId)
            .mapCatching { response -> check(response.success) { response.message } }

    override suspend fun sendPhotoMessage(
        roomId: Long,
        photoId: Long,
        message: String,
    ): ChallaResult<Unit> =
        postReaction(
            roomId = roomId,
            photoId = photoId,
            type = CreateChatRequest.ChatType.COMMENT,
            content = message,
        ).mapCatching { }

    private suspend fun postReaction(
        roomId: Long,
        photoId: Long,
        type: CreateChatRequest.ChatType,
        content: String,
    ) = chatApi
        .postChatForReaction(
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
}
