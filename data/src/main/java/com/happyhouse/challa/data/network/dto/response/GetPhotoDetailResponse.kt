package com.happyhouse.challa.data.network.dto.response

import com.happyhouse.challa.data.network.parseServerInstant
import com.happyhouse.challa.domain.model.PhotoReaction
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.orhanobut.logger.Logger
import kotlinx.serialization.Serializable

@Serializable
data class GetPhotoDetailResponse(
    val photo: Photo,
) {
    @Serializable
    data class Photo(
        val id: Long,
        val chats: List<Chat>,
    )

    /**
     * 사진에 달린 반응·댓글 한 건.
     *
     * 스웨거상 [id]와 [createdAt]은 optional이지만, 저장된 기록이라면 반드시 있는 값이라 non-null로 둔다.
     * 서버가 빼고 내려주면 파싱이 통째로 실패하므로, 그때는 서버 스펙을 고치는 쪽이 맞다.
     */
    @Serializable
    data class Chat(
        val id: Long,
        val type: String,
        val content: String,
        val userId: Long,
        val createdAt: String,
    )
}

/** 이모지 반응만 남긴 시각 오름차순으로 추린다. */
fun GetPhotoDetailResponse.toPhotoReactions(): List<PhotoReaction> =
    photo.chats
        .filter { chat -> chat.type == EMOJI_CHAT_TYPE }
        .mapNotNull { chat -> chat.toPhotoReactionOrNull() }
        .sortedBy { reaction -> reaction.createdAtEpochMillis }

private fun GetPhotoDetailResponse.Chat.toPhotoReactionOrNull(): PhotoReaction? {
    // 상위 버전에서 추가된 이모지는 그릴 그림이 없어 건너뛴다. 조용히 사라지지 않게 로그를 남긴다.
    val emoji =
        ReactionEmoji.from(content) ?: run {
            Logger.w("모르는 반응 이모지라 표시하지 않습니다: chatId=$id, content=$content")
            return null
        }

    return PhotoReaction(
        chatId = id,
        userId = userId,
        emoji = emoji,
        createdAtEpochMillis = createdAt.parseServerInstant().toEpochMilli(),
    )
}

private const val EMOJI_CHAT_TYPE = "EMOJI"
