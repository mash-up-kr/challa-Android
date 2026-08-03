package com.happyhouse.challa.presentation.home.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface RoomUiModel {
    val id: String
    val name: String
    val participantCount: Int

    /** 촬영 중 — 촬영한 사진 수와 커버 이미지 표기 */
    @Immutable
    data class Shooting(
        override val id: String,
        override val name: String,
        override val participantCount: Int,
        val takenCount: Int,
        val coverImageUrl: String?,
    ) : RoomUiModel

    /** 촬영 완료 — 인화 상태와 필름 미리보기 표기 */
    @Immutable
    data class Completed(
        override val id: String,
        override val name: String,
        override val participantCount: Int,
        val printState: PrintState,
        val photoImageUrls: ImmutableList<String>,
        val totalPhotoCount: Int,
    ) : RoomUiModel
}
