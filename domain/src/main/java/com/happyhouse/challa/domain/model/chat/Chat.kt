package com.happyhouse.challa.domain.model.chat

import java.time.Instant

/** 채팅방에 표시하는 채팅 한 건 */
sealed interface Chat {
    val id: Long
    val userId: Long
    val createdAt: Instant
    val userName: String?
    val userProfileImageUrl: String?

    data class Default(
        override val id: Long,
        override val userId: Long,
        val content: String,
        override val createdAt: Instant,
        override val userName: String?,
        override val userProfileImageUrl: String?,
    ) : Chat

    data class Emoji(
        override val id: Long,
        override val userId: Long,
        val content: String,
        val photoId: Long,
        val photoImageUrl: String,
        override val createdAt: Instant,
        override val userName: String?,
        override val userProfileImageUrl: String?,
    ) : Chat

    data class Comment(
        override val id: Long,
        override val userId: Long,
        val content: String,
        val photoId: Long,
        val photoImageUrl: String,
        override val createdAt: Instant,
        override val userName: String?,
        override val userProfileImageUrl: String?,
    ) : Chat
}
