package com.happyhouse.challa.presentation.home.model

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.domain.model.Room
import com.happyhouse.challa.domain.model.RoomStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

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

fun Room.toUiModel(): RoomUiModel? =
    when (status) {
        RoomStatus.SHOOTING ->
            RoomUiModel.Shooting(
                id = id.toString(),
                name = title,
                participantCount = memberCount,
                // "촬영한 사진 수" = 전체 장수 - 남은 장수
                takenCount = (totalPhotoCount - remainedPhotoCount).coerceAtLeast(0),
                coverImageUrl = thumbnailImageUrls.firstOrNull(),
            )

        RoomStatus.PHOTO_PRINT_PENDING,
        RoomStatus.PHOTO_PRINT_COMPLETED,
        ->
            RoomUiModel.Completed(
                id = id.toString(),
                name = title,
                participantCount = memberCount,
                printState = status.toPrintState(),
                photoImageUrls = thumbnailImageUrls.toImmutableList(),
                totalPhotoCount = totalPhotoCount,
            )

        RoomStatus.UNKNOWN -> null
    }

private fun RoomStatus.toPrintState(): PrintState =
    when (this) {
        RoomStatus.PHOTO_PRINT_COMPLETED -> PrintState.COMPLETED
        else -> PrintState.WAITING
    }
