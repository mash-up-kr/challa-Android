package com.happyhouse.challa.domain.model.chat

/** 채팅 목록 한 페이지 */
data class ChatPage(
    val chats: List<Chat>,
    val hasNext: Boolean,
)
