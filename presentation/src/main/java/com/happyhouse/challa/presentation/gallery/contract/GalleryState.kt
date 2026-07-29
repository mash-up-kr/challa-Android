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

        /**
         * 인화 전: 사진이 아직 공개되지 않아 번호만 있는 빈 필름 슬롯을 보여준다.
         */
        data class Waiting(
            val slotCount: Int,
            val remainingSeconds: Long,
        ) : PhotoInfo

        /**
         * 인화 완료: 공개된 사진을 그리드로 보여준다.
         */
        data class Printed(
            val photos: ImmutableList<GalleryPhotoUiModel>,
        ) : PhotoInfo
    }
}

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
