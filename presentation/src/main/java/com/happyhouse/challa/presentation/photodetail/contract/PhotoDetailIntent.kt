package com.happyhouse.challa.presentation.photodetail.contract

import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.presentation.base.UiIntent

sealed interface PhotoDetailIntent : UiIntent {
    /** 마지막 사진이 가까워져 다음 사진 페이지가 필요할 때 */
    data object PhotosLoadMore : PhotoDetailIntent

    /** 보고 있는 사진이 바뀌어 그 사진의 반응이 필요할 때 */
    data class ReactionsLoad(
        val photo: PhotoDetailUiModel,
    ) : PhotoDetailIntent

    data class PhotoSave(
        val photo: PhotoDetailUiModel,
    ) : PhotoDetailIntent

    data class ReactionClick(
        val photo: PhotoDetailUiModel,
        val emoji: ReactionEmoji,
    ) : PhotoDetailIntent

    data class MessageChange(
        val message: String,
    ) : PhotoDetailIntent

    data class MessageSend(
        val photo: PhotoDetailUiModel,
    ) : PhotoDetailIntent
}
