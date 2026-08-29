package com.happyhouse.challa.domain.model.chat

enum class ChatType {
    /** 일반 채팅 */
    DEFAULT,

    /** 사진에 남기는 이모지 */
    EMOJI,

    /** 사진에 남기는 댓글 */
    COMMENT,
}
