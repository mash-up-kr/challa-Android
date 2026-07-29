package com.happyhouse.challa.presentation.gallery.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface GallerySideEffect : UiSideEffect {
    data class NavigateToPhotoDetail(
        val photoId: Long,
    ) : GallerySideEffect

    /**
     * 아직 인화가 끝나지 않아 사진을 볼 수 없는 상태
     */
    data object PrintWaiting : GallerySideEffect
}
