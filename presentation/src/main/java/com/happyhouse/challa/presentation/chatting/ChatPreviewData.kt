package com.happyhouse.challa.presentation.chatting

import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.presentation.chatting.model.ChatUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.ZoneId
import java.time.ZonedDateTime

internal fun previewChats(photoImageUrl: String): ImmutableList<ChatUiModel> {
    val firstChatTime =
        ZonedDateTime.of(
            2026,
            8,
            29,
            20,
            15,
            0,
            0,
            ZoneId.systemDefault(),
        )

    return persistentListOf(
        ChatUiModel.Default(
            chatId = 1L,
            userId = 1L,
            content = "강릉에 도착하면 바로 사진 찍으러 가자!",
            createdAt = firstChatTime,
            isMine = false,
            userName = "그린그린여성현",
            userProfileImageUrl = null,
        ),
        ChatUiModel.Default(
            chatId = 2L,
            userId = 2L,
            content = "좋아! 바다부터 보고 숙소로 이동하자.",
            createdAt = firstChatTime.plusMinutes(1),
            isMine = true,
            userName = "찰나",
            userProfileImageUrl = null,
        ),
        ChatUiModel.Comment(
            chatId = 3L,
            userId = 1L,
            content = "이 사진 분위기 정말 좋다.",
            photoImageUrl = photoImageUrl,
            createdAt = firstChatTime.plusMinutes(2),
            isMine = false,
            userName = "그린그린여성현",
            userProfileImageUrl = null,
        ),
        ChatUiModel.Comment(
            chatId = 4L,
            userId = 2L,
            content = "나도 이 사진이 제일 마음에 들어.",
            photoImageUrl = photoImageUrl,
            createdAt = firstChatTime.plusMinutes(3),
            isMine = true,
            userName = "찰나",
            userProfileImageUrl = null,
        ),
        ChatUiModel.Emoji(
            chatId = 5L,
            userId = 1L,
            reactionEmoji = ReactionEmoji.POOP,
            photoImageUrl = photoImageUrl,
            createdAt = firstChatTime.plusDays(1),
            isMine = false,
            userName = "그린그린여성현",
            userProfileImageUrl = null,
        ),
        ChatUiModel.Emoji(
            chatId = 6L,
            userId = 2L,
            reactionEmoji = ReactionEmoji.FIRE,
            photoImageUrl = photoImageUrl,
            createdAt = firstChatTime.plusDays(1).plusMinutes(1),
            isMine = true,
            userName = "찰나",
            userProfileImageUrl = null,
        ),
    )
}
