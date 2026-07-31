package com.happyhouse.challa.presentation.gallery.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface GallerySideEffect : UiSideEffect {
    data class NavigateToPhotoDetail(
        val photoId: Long,
    ) : GallerySideEffect

    data object NavigateToCamera : GallerySideEffect

    /** 인화 대기 중에 남은 시간 버튼을 눌렀을 때. 아직 인화가 끝나지 않았음을 알린다. */
    data object PrintNotCompleted : GallerySideEffect
}
