package com.happyhouse.challa.presentation.photodetail.contract

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
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
        ) : PhotoInfo {
            fun reactionsOf(photoId: Long): ImmutableList<PhotoReactionUiModel> = reactions[photoId] ?: persistentListOf()
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
 * @param id 사진 위 노출 좌표를 뽑는 seed로도 쓴다. 같은 반응은 항상 같은 자리에 그려져야 한다.
 */
@Immutable
data class PhotoReactionUiModel(
    val id: Long,
    val emoji: ReactionEmoji,
)

/**
 * 한 사진에 붙일 수 있는 반응 수.
 *
 * 이 값을 늘리려면 스티커를 놓을 자리(`StickerSlotSet`)도 함께 늘려야 한다.
 * 자리가 모자라면 넘치는 반응은 화면에 그려지지 않는다.
 *
 * TODO: 참여자 전원 합산 기준으로 두고 기획 확인 중. (이슈 #110)
 */
const val MAX_REACTION_COUNT = 3

/** 반응 바에 노출하는 이모지 종류. 선언 순서가 곧 노출 순서이자 스와이프 페이지 순서다. */
enum class ReactionEmoji {
    FIRE,
    EYES,
    MEDAL,
    QUESTION,
    THINKING,
    HEART,
    THUMBS_UP,
    SPARKLES,
    POOP,
    SKULL,
}
