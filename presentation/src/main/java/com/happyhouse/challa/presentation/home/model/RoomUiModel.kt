package com.happyhouse.challa.presentation.home.model

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.domain.model.Room
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.presentation.home.model.RoomUiModel.Completed
import com.happyhouse.challa.presentation.home.model.RoomUiModel.Printing
import com.happyhouse.challa.presentation.home.model.RoomUiModel.Shooting
import com.happyhouse.challa.presentation.roomcover.model.RoomCoverUiModel
import com.happyhouse.challa.presentation.roomcover.model.toCoverUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber
import java.time.Instant

@Immutable
sealed interface RoomUiModel {
    val id: Long
    val name: String
    val participantCount: Int

    /** 촬영 중 — 촬영 배지와 방 커버 표기 */
    @Immutable
    data class Shooting(
        override val id: Long,
        override val name: String,
        override val participantCount: Int,
        val cover: RoomCoverUiModel,
        val firstPhotoImageUrl: String?,
    ) : RoomUiModel {
        /** 카드에 그릴 커버. 배경을 따로 지정하지 않은 방은 찍어둔 첫 사진을 대신 깐다. */
        val displayCover: RoomCoverUiModel
            get() = cover.copy(imageUrl = cover.imageUrl ?: firstPhotoImageUrl)
    }

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
                cover = cover.toCoverUiModel(),
                firstPhotoImageUrl = thumbnailImageUrls.firstOrNull(),
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

        // 앱이 모르는 상태(예: 서버가 새로 추가한 상태)는 그릴 방법이 없으므로 목록에서 뺀다.
        // 방 하나 때문에 홈 전체를 실패로 돌리지는 않는다.
        RoomStatus.UNKNOWN -> {
            Timber.w("방 상태를 해석하지 못해 홈 목록에서 제외합니다. roomId=$id")
            null
        }
    }
