package com.happyhouse.challa.presentation.gallery

import com.happyhouse.challa.presentation.gallery.contract.GalleryFilmSlotUiModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryMemberUiModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryPhotoUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

/** @Preview 전용 남은 시간 (2:59:58) */
internal const val PREVIEW_REMAINING_SECONDS = 10_798L

/**
 * @Preview 전용 mock 참여자 목록
 */
internal fun previewGalleryMembers(count: Int = 6): ImmutableList<GalleryMemberUiModel> =
    (0 until count)
        .map { index ->
            GalleryMemberUiModel(
                id = index.toLong(),
                profileImageUrl = "",
            )
        }.toPersistentList()

/**
 * @Preview 전용 mock 필름 슬롯 목록
 */
internal fun previewGalleryFilmSlots(count: Int = 24): ImmutableList<GalleryFilmSlotUiModel> =
    (0 until count)
        .map { index ->
            GalleryFilmSlotUiModel(
                order = index + 1,
                imageUrl = null,
            )
        }.toPersistentList()

/**
 * @Preview 전용 mock 사진 목록
 */
internal fun previewGalleryPhotos(count: Int = 24): ImmutableList<GalleryPhotoUiModel> =
    (0 until count)
        .map { index ->
            GalleryPhotoUiModel(
                id = index.toLong(),
                order = index + 1,
                imageUrl = "",
            )
        }.toPersistentList()
