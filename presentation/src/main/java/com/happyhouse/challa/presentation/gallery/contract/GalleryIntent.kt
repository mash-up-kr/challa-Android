package com.happyhouse.challa.presentation.gallery.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface GalleryIntent : UiIntent {
    // 최초 진입 및 에러 후 재시도에 모두 사용한다.
    data object PhotosLoad : GalleryIntent

    data class PhotoClick(
        val photoId: Long,
    ) : GalleryIntent
}
