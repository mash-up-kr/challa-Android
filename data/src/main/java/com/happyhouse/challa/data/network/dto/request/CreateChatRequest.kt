package com.happyhouse.challa.data.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateChatRequest(
    val chat: Chat,
) {
    @Serializable
    data class Chat(
        val roomId: Long,
        val photoId: Long,
        val type: ChatType,
        val content: String,
    )

    /** 서버에는 방 전체 채팅용 `DEFAULT`도 있지만, 앱이 이 요청으로 보내는 건 사진 반응·메시지뿐이다. */
    @Serializable
    enum class ChatType {
        EMOJI,
        COMMENT,
    }
}
