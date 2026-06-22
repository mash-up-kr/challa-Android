package com.happyhouse.challa.presentation.photodetail.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface PhotoDetailIntent : UiIntent {
    // 최초 진입 및 에러 후 재시도에 모두 사용한다.
    data object PhotosLoad : PhotoDetailIntent

    data class PhotoSave(
        val photoId: Long,
    ) : PhotoDetailIntent

    data class PhotoSaveResult(
        val isSuccess: Boolean,
    ) : PhotoDetailIntent
}
