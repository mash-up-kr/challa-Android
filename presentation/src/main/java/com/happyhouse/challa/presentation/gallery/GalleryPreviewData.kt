package com.happyhouse.challa.presentation.gallery

import com.happyhouse.challa.presentation.gallery.contract.GalleryFilmSlotUiModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryMemberUiModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryPhotoUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

/** @Preview 전용 남은 시간 (2:59:58) */
internal const val PREVIEW_REMAINING_SECONDS = 10_798L

/** @Preview 전용 필름 총 칸 수 */
internal const val PREVIEW_FILM_SLOT_COUNT = 24

/** @Preview 전용 mock 참여자 닉네임. 참여자 수가 이보다 많으면 앞에서부터 다시 쓴다. */
private val PREVIEW_MEMBER_NICKNAMES =
    listOf("부리부리자에몽", "감자도리", "말차라떼", "우주먼지", "구름과자", "노랑노랑")

/**
 * @Preview 전용 mock 참여자 목록
 */
internal fun previewGalleryMembers(count: Int = 6): ImmutableList<GalleryMemberUiModel> =
    (0 until count)
        .map { index ->
            GalleryMemberUiModel(
                id = index.toLong(),
                nickname = PREVIEW_MEMBER_NICKNAMES[index % PREVIEW_MEMBER_NICKNAMES.size],
                profileImageUrl = "",
            )
        }.toPersistentList()

/**
 * @Preview 전용 mock 필름 슬롯 목록
 *
 * @param capturedCount 앞에서부터 촬영된 칸 수. 나머지는 빈 슬롯이다.
 */
internal fun previewGalleryFilmSlots(
    count: Int = PREVIEW_FILM_SLOT_COUNT,
    capturedCount: Int = 0,
): ImmutableList<GalleryFilmSlotUiModel> =
    (0 until count)
        .map { index ->
            GalleryFilmSlotUiModel(
                order = index + 1,
                state =
                    if (index < capturedCount) {
                        GalleryFilmSlotUiModel.State.Captured(imageUrl = "")
                    } else {
                        GalleryFilmSlotUiModel.State.Empty
                    },
            )
        }.toPersistentList()

/**
 * @Preview 전용 mock 사진 목록
 */
internal fun previewGalleryPhotos(count: Int = PREVIEW_FILM_SLOT_COUNT): ImmutableList<GalleryPhotoUiModel> =
    (0 until count)
        .map { index ->
            GalleryPhotoUiModel(
                id = index.toLong(),
                order = index + 1,
                imageUrl = "",
            )
        }.toPersistentList()
