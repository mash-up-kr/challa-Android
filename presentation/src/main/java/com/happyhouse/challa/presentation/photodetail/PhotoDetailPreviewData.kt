package com.happyhouse.challa.presentation.photodetail

import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

/**
 * @Preview 전용 mock 사진 목록
 */
internal fun previewPhotoDetailPhotos(count: Int = 3): ImmutableList<PhotoDetailUiModel> =
    (0 until count)
        .map { index ->
            PhotoDetailUiModel(
                id = index.toLong(),
                imageUrl = "",
                photographer = "이주연",
                capturedDate = "Oct 12 2026",
            )
        }.toPersistentList()
