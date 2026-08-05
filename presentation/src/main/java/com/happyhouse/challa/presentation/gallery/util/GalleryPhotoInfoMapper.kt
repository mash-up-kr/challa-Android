package com.happyhouse.challa.presentation.gallery.util

import com.happyhouse.challa.domain.model.Photo
import com.happyhouse.challa.domain.model.RoomDetail
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.presentation.gallery.contract.GalleryFilmSlotUiModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryPhotoUiModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryState.PhotoInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

/**
 * 방 상태와 사진 목록을 갤러리 본문 상태로 옮긴다.
 *
 * @param remainingSeconds 인화 완료까지 남은 시간. 인화 대기가 아니면 쓰이지 않는다.
 */
internal fun RoomDetail.toPhotoInfo(
    photos: List<Photo>,
    remainingSeconds: Long,
): PhotoInfo =
    when (status) {
        RoomStatus.SHOOTING -> PhotoInfo.Shooting(slots = toFilmSlots(photos))

        RoomStatus.PHOTO_PRINT_PENDING ->
            PhotoInfo.Waiting(
                slots = toFilmSlots(photos),
                remainingSeconds = remainingSeconds,
            )

        RoomStatus.PHOTO_PRINT_COMPLETED -> PhotoInfo.Printed(photos = photos.toGalleryPhotos())
    }

/**
 * 필름은 방의 전체 칸 수만큼 그리고, 앞에서부터 받아온 사진으로 채운다.
 * 아직 찍지 않은 칸과, 서버가 이미지를 감춘 칸은 모두 빈 자리로 남는다.
 */
private fun RoomDetail.toFilmSlots(photos: List<Photo>): ImmutableList<GalleryFilmSlotUiModel> =
    (0 until totalPhotoCount)
        .map { index ->
            GalleryFilmSlotUiModel(
                order = index + 1,
                imageUrl = photos.getOrNull(index)?.imageUrl,
            )
        }.toPersistentList()

/** 인화가 끝나 공개된 사진만 그린다. 이미지가 없는 사진은 띄울 것이 없으므로 제외한다. */
private fun List<Photo>.toGalleryPhotos(): ImmutableList<GalleryPhotoUiModel> =
    mapNotNull { photo -> photo.imageUrl?.let { imageUrl -> photo.id to imageUrl } }
        .mapIndexed { index, (id, imageUrl) ->
            GalleryPhotoUiModel(
                id = id,
                order = index + 1,
                imageUrl = imageUrl,
            )
        }.toPersistentList()
