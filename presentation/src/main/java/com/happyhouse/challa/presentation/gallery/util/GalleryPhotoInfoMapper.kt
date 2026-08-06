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
 * 필름은 방의 전체 칸 수만큼 그리고, 앞에서부터 촬영된 칸을 채운다.
 *
 * 촬영 여부는 서버가 내려준 촬영 수(`totalPhotoCount - remainedPhotoCount`)로 판단한다.
 * 인화 전에는 사진의 `imageUrl`이 비어 올 수 있어, 이미지 유무로만 판단하면
 * 이미 찍은 칸까지 촬영 전으로 보이기 때문이다.
 */
private fun RoomDetail.toFilmSlots(photos: List<Photo>): ImmutableList<GalleryFilmSlotUiModel> {
    val capturedCount = (totalPhotoCount - remainedPhotoCount).coerceIn(0, totalPhotoCount)

    return (0 until totalPhotoCount)
        .map { index ->
            GalleryFilmSlotUiModel(
                order = index + 1,
                state =
                    if (index < capturedCount) {
                        GalleryFilmSlotUiModel.State.Captured(imageUrl = photos.getOrNull(index)?.imageUrl)
                    } else {
                        GalleryFilmSlotUiModel.State.Empty
                    },
            )
        }.toPersistentList()
}

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
