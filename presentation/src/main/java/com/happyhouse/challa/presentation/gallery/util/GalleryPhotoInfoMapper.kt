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
 * 인화 전 사진은 서버가 원본 `imageUrl` 을 내려주고 앱이 블러 처리해 보여준다.
 * 따라서 촬영된 칸에 이미지가 없는 것은 정상 흐름이 아니므로, 조용히 빈 칸으로 두지 않고 경고를 남긴다.
 */
private fun RoomDetail.toFilmSlots(photos: List<Photo>): ImmutableList<GalleryFilmSlotUiModel> {
    val capturedCount = (totalPhotoCount - remainedPhotoCount).coerceIn(0, totalPhotoCount)

    val missingImageCount = (0 until capturedCount).count { photos.getOrNull(it)?.imageUrl == null }
    if (missingImageCount > 0) {
        Timber.w(
            "촬영된 칸 ${capturedCount}개 중 ${missingImageCount}개에 이미지가 없어 블러를 그리지 못합니다. roomId=$id",
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
 * 인화가 끝나 공개된 사진만 그린다. 이미지가 없는 사진은 띄울 것이 없으므로 제외한다.
 *
 * 번호는 걸러내기 전 원래 자리로 매긴다. 거른 뒤 다시 세면 빠진 사진 뒤의 번호가 앞당겨져
 * 촬영 순서와 어긋나기 때문이다.
 */
private fun List<Photo>.toGalleryPhotos(): ImmutableList<GalleryPhotoUiModel> =
    mapIndexedNotNull { index, photo ->
        photo.imageUrl?.let { imageUrl ->
            GalleryPhotoUiModel(
                id = photo.id,
                order = index + 1,
                imageUrl = imageUrl,
            )
        }
    }.toPersistentList()
