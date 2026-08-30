package com.happyhouse.challa.data.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class CreateChatResponse(
    val chat: Chat,
) {
    /** 보낸 내용을 그대로 돌려주므로 취소에 필요한 [chatId]만 쓴다. */
    @Serializable
    data class Chat(
        val chatId: Long,
    )
}
