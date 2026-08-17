package com.happyhouse.challa.presentation.photodetail

import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

/** @Preview 전용 mock 사진 목록 */
internal fun previewPhotoDetailPhotos(
    count: Int = 3,
    hasProfileImage: Boolean = false,
): ImmutableList<PhotoDetailUiModel> =
    (0 until count)
        .map { index ->
            PhotoDetailUiModel(
                id = index.toLong(),
                imageUrl = "",
                photographer = "나는야멋쟁이토마토",
                photographerProfileImageUrl = PREVIEW_PROFILE_IMAGE_URL.takeIf { hasProfileImage },
                capturedDate = "2026. 7. 16. 14:34",
            )
        }.toPersistentList()

/** 프리뷰에서는 로딩에 실패해 기본 프로필 아이콘이 그려진다. */
private const val PREVIEW_PROFILE_IMAGE_URL = "https://challa.example.com/profile.png"
