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

    /**
     * 서버에는 방 전체 채팅용 `DEFAULT`와 이모지 반응용 `EMOJI`도 있지만,
     * 지금 앱에서 보내는 건 사진 메시지뿐이라 쓰는 값만 둔다.
     */
    @Serializable
    enum class ChatType {
        /** 사진에 남기는 메시지 */
        COMMENT,
    }
}
