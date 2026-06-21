package com.happyhouse.challa.presentation.gallery.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class GalleryState(
    val roomId: Long = 0L,
    val roomName: String = "",
    val isLoading: Boolean = true,
    val photos: ImmutableList<GalleryPhotoUiModel> = persistentListOf(),
    val remainingDays: Int = 3,
    val isError: Boolean = false,
) : UiState {
    val isEmpty: Boolean
        get() = !isLoading && !isError && photos.isEmpty()
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
