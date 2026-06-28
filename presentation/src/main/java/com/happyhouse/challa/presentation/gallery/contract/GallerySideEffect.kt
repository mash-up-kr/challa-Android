package com.happyhouse.challa.presentation.gallery.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface GallerySideEffect : UiSideEffect {
    data class NavigateToPhotoDetail(
        val photoId: Long,
    ) : GallerySideEffect
}
