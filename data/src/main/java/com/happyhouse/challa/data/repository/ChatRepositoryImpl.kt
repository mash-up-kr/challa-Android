package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.network.api.ChatApi
import com.happyhouse.challa.data.network.dto.request.CreateChatRequest
import com.happyhouse.challa.domain.repository.ChatRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatApi: ChatApi,
) : ChatRepository {
    override suspend fun sendPhotoMessage(
        roomId: Long,
        photoId: Long,
        message: String,
    ): ChallaResult<Unit> =
        chatApi
            .postChatForReaction(
                CreateChatRequest(
                    chat =
                        CreateChatRequest.Chat(
                            roomId = roomId,
                            photoId = photoId,
                            type = CreateChatRequest.ChatType.COMMENT,
                            content = message,
                        ),
                ),
            ).mapCatching { response ->
                check(response.success) { response.message }
            }
}
