package com.happyhouse.challa.presentation.photodetail.contract

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.parcelize.Parcelize

@Immutable
data class PhotoDetailState(
    val roomId: Long = 0L,
    val initialPhotoId: Long = 0L,
    val roomName: String = "",
    val photoInfo: PhotoInfo = PhotoInfo.Loading,
    val isSaving: Boolean = false,
) : UiState {
    @Immutable
    sealed interface PhotoInfo {
        data object Loading : PhotoInfo

        data object Error : PhotoInfo

        data object Empty : PhotoInfo

        data class Loaded(
            val photos: ImmutableList<PhotoDetailUiModel>,
        ) : PhotoInfo
    }
}

@Immutable
@Parcelize
data class PhotoDetailUiModel(
    val id: Long,
    val imageUrl: String,
    val photographer: String,
    val capturedDate: String,
) : Parcelable
