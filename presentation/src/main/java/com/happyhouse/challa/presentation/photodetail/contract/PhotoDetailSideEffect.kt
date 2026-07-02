package com.happyhouse.challa.presentation.photodetail.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface PhotoDetailSideEffect : UiSideEffect {
    data class SavePhotoToDevice(
        val imageUrl: String,
    ) : PhotoDetailSideEffect
}
