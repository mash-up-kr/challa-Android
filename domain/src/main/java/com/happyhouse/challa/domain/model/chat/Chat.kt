package com.happyhouse.challa.domain.model.chat

import java.time.Instant

/** 채팅방에 표시하는 채팅 한 건 */
data class Chat(
    val userId: Long,
    val type: ChatType,
    val content: String,
    val photoId: Long?,
    val photoImageUrl: String?,
    val createdAt: Instant,
    val userName: String?,
    val userProfileImageUrl: String?,
)
