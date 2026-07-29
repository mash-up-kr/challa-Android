package com.happyhouse.challa.presentation.gallery.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class GalleryState(
    val roomId: Long = 0L,
    val roomName: String = "",
    val photoInfo: PhotoInfo = PhotoInfo.Loading,
) : UiState {
    @Immutable
    sealed interface PhotoInfo {
        data object Loading : PhotoInfo

        data object Error : PhotoInfo

        data object Empty : PhotoInfo

        data class Loaded(
            val photos: ImmutableList<GalleryPhotoUiModel>,
        ) : PhotoInfo
    }
}

/**
 * 갤러리 썸네일 UI 모델
 */
@Immutable
data class GalleryPhotoUiModel(
    val id: Long,
    val order: Int,
    val imageUrl: String,
)
