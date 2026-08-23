package com.happyhouse.challa.presentation.photodetail.contract

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.presentation.base.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.parcelize.Parcelize

@Immutable
data class PhotoDetailState(
    val roomId: Long = 0L,
    val initialPhotoId: Long = 0L,
    val roomName: String = "",
    val photoInfo: PhotoInfo = PhotoInfo.Loading,
    val isSaving: Boolean = false,
    val messageInput: String = "",
    val isSendingMessage: Boolean = false,
) : UiState {
    val isMessageSendable: Boolean get() = messageInput.isNotBlank() && !isSendingMessage

    @Immutable
    sealed interface PhotoInfo {
        data object Loading : PhotoInfo

        data object Error : PhotoInfo

        data object Empty : PhotoInfo

        /**
         * @param reactions 사진 id별 반응 목록. 반응은 사진이 있을 때만 존재하므로 Loaded 안에 둔다.
         *   [PhotoDetailUiModel]은 rememberSaveable에 담기느라 Parcelable이라 반응을 직접 갖지 못한다.
         */
        data class Loaded(
            val photos: ImmutableList<PhotoDetailUiModel>,
            val reactions: ImmutableMap<Long, ImmutableList<PhotoReactionUiModel>> = persistentMapOf(),
            /** 사진별로 내가 남겨둔 이모지. 반응 바에 표시하고, 다시 누르면 취소한다. */
            val myEmojis: ImmutableMap<Long, ImmutableSet<ReactionEmoji>> = persistentMapOf(),
            val burst: ReactionBurstUiModel? = null,
        ) : PhotoInfo {
            fun reactionsOf(photoId: Long): ImmutableList<PhotoReactionUiModel> = reactions[photoId] ?: persistentListOf()

            fun myEmojisOf(photoId: Long): ImmutableSet<ReactionEmoji> = myEmojis[photoId] ?: persistentSetOf()
        }
    }
}

/**
 * 사진 상세 페이지 한 장
 *
 * @param photographerProfileImageUrl 촬영자 프로필 사진. 없으면 null이고 화면에서 기본 프로필 아이콘을 그린다.
 */
@Immutable
@Parcelize
data class PhotoDetailUiModel(
    val id: Long,
    val imageUrl: String,
    val photographer: String,
    val photographerProfileImageUrl: String?,
    val capturedDate: String,
) : Parcelable

/**
 * 사진 위에 붙는 스티커 하나.
 *
 * @param chatId 취소에 쓰고, 배치 좌표를 뽑는 seed로도 쓴다. 같은 반응은 항상 같은 자리에 그려진다.
 */
@Immutable
data class PhotoReactionUiModel(
    val chatId: Long,
    val emoji: ReactionEmoji,
)

/** 연출이 끝나면 상태에서 지운다. 남겨두면 사진을 다시 열 때 또 재생된다. */
const val REACTION_BURST_DURATION_MILLIS = 1100L

/**
 * 반응을 남기는 순간 재생할 연출.
 *
 * @param id 연출을 다시 트리거하는 키. 같은 이모지를 또 남겨도 값이 달라야 다시 재생된다.
 * @param photoId 어느 사진 위에서 터뜨릴지
 */
@Immutable
data class ReactionBurstUiModel(
    val id: Long,
    val photoId: Long,
    val emoji: ReactionEmoji,
)

/**
 * 사진 한 장에 스티커로 보여주는 사람 수.
 *
 * 인당 반응 개수에는 제한이 없고, 사람마다 **가장 먼저 남긴 반응 하나**만 스티커가 된다.
 * 먼저 남긴 순으로 이 수만큼만 붙고 나머지는 채팅 기록에만 쌓인다.
 *
 * 이 값을 늘리려면 스티커를 놓을 자리(`StickerSlotSet`)도 함께 늘려야 한다.
 */
const val MAX_STICKER_USER_COUNT = 3
