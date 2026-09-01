package com.happyhouse.challa.presentation.home.model

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.domain.model.Room
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.presentation.home.model.RoomUiModel.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.time.Instant

@Immutable
sealed interface RoomUiModel {
    val id: Long
    val name: String
    val participantCount: Int

    /** 촬영 중 — 촬영 배지와 커버 이미지 표기 */
    @Immutable
    data class Shooting(
        override val id: Long,
        override val name: String,
        override val participantCount: Int,
        val coverImageUrl: String?,
    ) : RoomUiModel

    /** 인화 전 — 촬영을 마치고 인화 완료까지 남은 시간을 세는 상태(커버로 가려진 사진) */
    @Immutable
    data class Printing(
        override val id: Long,
        override val name: String,
        override val participantCount: Int,
        val coverImageUrl: String?,
        /** 인화가 끝나는 시각. 아직 잡히지 않았으면 null */
        val printCompletedAt: Instant?,
    ) : RoomUiModel

    /** 인화 완료 — 필름 미리보기 표기 */
    @Immutable
    data class Completed(
        override val id: Long,
        override val name: String,
        override val participantCount: Int,
        val photoImageUrls: ImmutableList<String>,
        val totalPhotoCount: Int,
        val hasUncheckedPrint: Boolean,
    ) : RoomUiModel
}

fun Room.toUiModel(): RoomUiModel? =
    when (status) {
        RoomStatus.SHOOTING ->
            Shooting(
                id = id,
                name = title,
                participantCount = memberCount,
                coverImageUrl = thumbnailImageUrls.firstOrNull(),
            )

        RoomStatus.PHOTO_PRINT_PENDING ->
            Printing(
                id = id,
                name = title,
                participantCount = memberCount,
                coverImageUrl = thumbnailImageUrls.firstOrNull(),
                printCompletedAt = photoPrintCompletedAt,
            )

        RoomStatus.PHOTO_PRINT_COMPLETED ->
            Completed(
                id = id,
                name = title,
                participantCount = memberCount,
                photoImageUrls = thumbnailImageUrls.toImmutableList(),
                totalPhotoCount = totalPhotoCount,
                hasUncheckedPrint = photoPrintCompletionCheckedAt == null,
            )

        RoomStatus.UNKNOWN -> TODO()
    }
