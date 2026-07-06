package com.happyhouse.challa.presentation.photodetail.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface PhotoDetailIntent : UiIntent {
    data object PhotosLoad : PhotoDetailIntent

    data class PhotoSave(
        val photo: PhotoDetailUiModel,
    ) : PhotoDetailIntent
}
