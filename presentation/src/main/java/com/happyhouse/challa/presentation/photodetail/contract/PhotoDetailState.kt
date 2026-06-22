package com.happyhouse.challa.presentation.photodetail.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class PhotoDetailState(
    val roomId: Long = 0L,
    val initialPhotoId: Long = 0L,
    val roomName: String = "",
    val photoInfo: PhotoInfo = PhotoInfo.Loading,
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
data class PhotoDetailUiModel(
    val id: Long,
    val imageUrl: String,
    val photographer: String,
    // TODO: 실제로는 촬영 timestamp를 받아 표시용 포맷으로 변환한다. 포맷/로케일은 디자인 확정 후 결정.
    val capturedDate: String,
)
