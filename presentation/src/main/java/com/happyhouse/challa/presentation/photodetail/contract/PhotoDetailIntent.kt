package com.happyhouse.challa.presentation.photodetail.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface PhotoDetailIntent : UiIntent {
    data object PhotosLoad : PhotoDetailIntent

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
