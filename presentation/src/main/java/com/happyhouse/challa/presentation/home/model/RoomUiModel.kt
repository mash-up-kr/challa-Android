package com.happyhouse.challa.presentation.home.model

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.domain.model.Room
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.presentation.roomcover.model.RoomCoverUiModel
import com.happyhouse.challa.presentation.roomcover.model.toCoverUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
sealed interface RoomUiModel {
    val id: Long
    val name: String
    val participantCount: Int

    /** 촬영 중 — 촬영한 사진 수와 커버 이미지 표기 */
    @Immutable
    data class Shooting(
        override val id: Long,
        override val name: String,
        override val participantCount: Int,
        val takenCount: Int,
        val cover: RoomCoverUiModel,
        val firstPhotoImageUrl: String?,
    ) : RoomUiModel {
        /**
         * 카드에 그릴 커버. 배경을 따로 지정하지 않은 방은 찍어둔 첫 사진을 대신 깐다.
         * [cover]가 아니라 이 값을 그려야 커버를 지웠을 때도 첫 사진으로 되돌아간다.
         */
        val displayCover: RoomCoverUiModel
            get() = cover.copy(imageUrl = cover.imageUrl ?: firstPhotoImageUrl)
    }

    /** 촬영 완료 — 인화 상태와 필름 미리보기 표기 */
    @Immutable
    data class Completed(
        override val id: Long,
        override val name: String,
        override val participantCount: Int,
        val printState: PrintState,
        val photoImageUrls: ImmutableList<String>,
        val totalPhotoCount: Int,
        val hasUncheckedPrint: Boolean,
    ) : RoomUiModel
}

/** 이름만 바꾼 사본. 타입마다 copy가 달라 여기서 한 번에 분기한다. */
fun RoomUiModel.withName(name: String): RoomUiModel =
    when (this) {
        is RoomUiModel.Shooting -> copy(name = name)
        is RoomUiModel.Completed -> copy(name = name)
    }

/** 커버만 바꾼 사본. 촬영 완료한 방은 카드에 커버를 쓰지 않는다. */
fun RoomUiModel.withCover(cover: RoomCoverUiModel): RoomUiModel =
    when (this) {
        is RoomUiModel.Shooting -> copy(cover = cover)
        is RoomUiModel.Completed -> this
    }

/** 인화 연출을 이미 본 것으로 표시한 사본. 촬영 중인 방은 표시할 것이 없다. */
fun RoomUiModel.withPrintChecked(): RoomUiModel =
    when (this) {
        is RoomUiModel.Shooting -> this
        is RoomUiModel.Completed -> copy(hasUncheckedPrint = false)
    }

fun Room.toUiModel(): RoomUiModel? =
    when (status) {
        RoomStatus.SHOOTING ->
            RoomUiModel.Shooting(
                id = id,
                name = title,
                participantCount = memberCount,
                // "촬영한 사진 수" = 전체 장수 - 남은 장수
                takenCount = (totalPhotoCount - remainedPhotoCount).coerceAtLeast(0),
                cover = cover.toCoverUiModel(),
                firstPhotoImageUrl = thumbnailImageUrls.firstOrNull(),
            )

        RoomStatus.PHOTO_PRINT_PENDING,
        RoomStatus.PHOTO_PRINT_COMPLETED,
        -> {
            val printState = status.toPrintState()

            RoomUiModel.Completed(
                id = id,
                name = title,
                participantCount = memberCount,
                printState = printState,
                photoImageUrls = thumbnailImageUrls.toImmutableList(),
                totalPhotoCount = totalPhotoCount,
                hasUncheckedPrint =
                    printState == PrintState.COMPLETED && photoPrintCompletionCheckedAt == null,
            )
        }

        RoomStatus.UNKNOWN -> null
    }

private fun RoomStatus.toPrintState(): PrintState =
    when (this) {
        RoomStatus.PHOTO_PRINT_COMPLETED -> PrintState.COMPLETED
        else -> PrintState.WAITING
    }
