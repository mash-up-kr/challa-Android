package com.happyhouse.challa.presentation.gallery.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface GalleryIntent : UiIntent {
    // 최초 진입 및 에러 후 재시도에 모두 사용한다.
    data object PhotosLoad : GalleryIntent

    /** 그리드 끝이 가까워져 다음 사진 페이지가 필요할 때 */
    data object PhotosLoadMore : GalleryIntent

    data class PhotoClick(
        val photoId: Long,
    ) : GalleryIntent

    data object PrintCountdownClick : GalleryIntent

    data object ShootClick : GalleryIntent
}
