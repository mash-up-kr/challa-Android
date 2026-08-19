package com.happyhouse.challa.data.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateChatRequest(
    val chat: Chat,
) {
    /** @param photoId 사진에 남기는 반응·메시지일 때만 채운다. 방 전체 채팅(DEFAULT)이면 null이다. */
    @Serializable
    data class Chat(
        val roomId: Long,
        val photoId: Long? = null,
        val type: ChatType,
        val content: String,
    )

    @Serializable
    enum class ChatType {
        /** 방 전체 채팅 */
        DEFAULT,

        /** 사진에 남기는 이모지 반응 */
        EMOJI,

        /** 사진에 남기는 메시지 */
        COMMENT,
    }
}
