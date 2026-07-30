package com.happyhouse.challa.presentation.gallery.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class GalleryState(
    val roomId: Long = 0L,
    val roomName: String = "",
    val members: ImmutableList<GalleryMemberUiModel> = persistentListOf(),
    val photoInfo: PhotoInfo = PhotoInfo.Loading,
) : UiState {
    @Immutable
    sealed interface PhotoInfo {
        data object Loading : PhotoInfo

        data object Error : PhotoInfo

        data object Empty : PhotoInfo

        /** 인화 전. 사진 대신 번호가 매겨진 필름 슬롯을 그린다. */
        @Immutable
        sealed interface Film : PhotoInfo {
            val slots: ImmutableList<GalleryFilmSlotUiModel>
        }

        /** 촬영 중. 필름을 다 채우지 못해 아직 인화 시각이 잡히지 않았다. */
        data class Shooting(
            override val slots: ImmutableList<GalleryFilmSlotUiModel>,
        ) : Film

        /** 인화 대기. 필름을 다 채워 인화 완료까지 남은 시간을 센다. */
        data class Waiting(
            override val slots: ImmutableList<GalleryFilmSlotUiModel>,
            val remainingSeconds: Long,
        ) : Film

        data class Printed(
            val photos: ImmutableList<GalleryPhotoUiModel>,
        ) : PhotoInfo
    }
}

/**
 * 인화 전 필름 슬롯 UI 모델
 *
 * @param imageUrl 아직 촬영되지 않은 자리는 null
 */
@Immutable
data class GalleryFilmSlotUiModel(
    val order: Int,
    val imageUrl: String?,
)

/**
 * 방 참여자 UI 모델
 */
@Immutable
data class GalleryMemberUiModel(
    val id: Long,
    val profileImageUrl: String,
)

/**
 * 갤러리 썸네일 UI 모델
 */
@Immutable
data class GalleryPhotoUiModel(
    val id: Long,
    val order: Int,
    val imageUrl: String,
)
