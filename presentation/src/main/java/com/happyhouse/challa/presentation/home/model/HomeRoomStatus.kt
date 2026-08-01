package com.happyhouse.challa.presentation.home.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface HomeRoomStatus {
    /** 촬영 중 — 촬영한 사진 수와 커버 이미지 표기 */
    @Immutable
    data class Shooting(
        val takenCount: Int,
        val coverImageUrl: String?,
    ) : HomeRoomStatus

    /** 촬영 완료 — 인화 상태와 필름 미리보기 표기 */
    @Immutable
    data class Completed(
        val printState: PrintState,
        val photoImageUrls: ImmutableList<String>,
        val totalPhotoCount: Int,
    ) : HomeRoomStatus
}
