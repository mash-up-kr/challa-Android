package com.happyhouse.challa.presentation.gallery.util

import com.happyhouse.challa.domain.model.Photo
import com.happyhouse.challa.domain.model.RoomDetail
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.presentation.gallery.contract.GalleryFilmSlotUiModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryPhotoUiModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryState.PhotoInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import timber.log.Timber

/**
 * 방 상태와 지금까지 받은 사진 목록을 갤러리 본문 상태로 옮긴다.
 *
 * @param remainingSeconds 인화 완료까지 남은 시간. 인화 대기가 아니면 쓰이지 않는다.
 * @param hasNextPhotoPage 아직 받지 않은 사진 페이지가 남았는지. 남았으면 사진이 촬영 수보다 적은 것이 정상이다.
 */
internal fun RoomDetail.toPhotoInfo(
    photos: List<Photo>,
    remainingSeconds: Long,
    hasNextPhotoPage: Boolean,
): PhotoInfo =
    when (status) {
        RoomStatus.SHOOTING -> PhotoInfo.Shooting(slots = toFilmSlots(photos, hasNextPhotoPage))

        RoomStatus.PHOTO_PRINT_PENDING ->
            PhotoInfo.Waiting(
                slots = toFilmSlots(photos, hasNextPhotoPage),
                remainingSeconds = remainingSeconds,
            )

        RoomStatus.PHOTO_PRINT_COMPLETED -> PhotoInfo.Printed(photos = photos.toGalleryPhotos())

        RoomStatus.UNKNOWN -> {
            Timber.w("방 상태를 해석하지 못해 갤러리를 에러로 표시합니다. roomId=$id")
            PhotoInfo.Error
        }
    }

/**
 * 필름은 방의 전체 칸 수만큼 그리고, 앞에서부터 촬영된 칸을 채운다.
 *
 * 촬영 여부는 서버가 내려준 촬영 수(`totalPhotoCount - remainedPhotoCount`)로 판단한다.
 * 사진 목록이 촬영 수보다 적게 와도 이미 찍은 칸이 촬영 전으로 보이지 않게 하기 위해서다.
 *
 * 인화 전 사진도 서버가 원본 `imageUrl` 을 내려주고 앱이 블러 처리해 보여준다.
 * 따라서 아직 받을 페이지가 없는데도 사진 목록이 촬영 수보다 짧은 것은 정상 흐름이 아니므로,
 * 조용히 빈 칸으로 두지 않고 경고를 남긴다.
 */
private fun RoomDetail.toFilmSlots(
    photos: List<Photo>,
    hasNextPhotoPage: Boolean,
): ImmutableList<GalleryFilmSlotUiModel> {
    val capturedCount = (totalPhotoCount - remainedPhotoCount).coerceIn(0, totalPhotoCount)

    if (!hasNextPhotoPage && photos.size < capturedCount) {
        Timber.w(
            "촬영된 칸 ${capturedCount}개 중 ${photos.size}개만 받아 나머지 칸에 블러를 그리지 못합니다. roomId=$id",
        )
    }

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

/**
 * 인화가 끝나 공개된 사진을 그린다. 번호는 촬영 순서대로 매긴다.
 */
private fun List<Photo>.toGalleryPhotos(): ImmutableList<GalleryPhotoUiModel> =
    mapIndexed { index, photo ->
        GalleryPhotoUiModel(
            id = photo.id,
            order = index + 1,
            imageUrl = photo.imageUrl,
        )
    }.toPersistentList()
