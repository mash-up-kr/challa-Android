package com.happyhouse.challa.presentation.model

import androidx.compose.runtime.Immutable
import kotlin.time.Duration

@Immutable
sealed interface RoomStatus {
    data class Shooting(
        val taken: Int,
        val total: Int,
    ) : RoomStatus

    data class Waiting(
        val dDay: Int,
        val remaining: Duration,
    ) : RoomStatus

    data object Opened : RoomStatus

    data class Expiring(
        val dDay: Int,
    ) : RoomStatus
}

val RoomStatus.label: String
    get() =
        when (this) {
            is RoomStatus.Shooting -> "촬영중"
            is RoomStatus.Waiting -> "대기중"
            RoomStatus.Opened -> "공개됨"
            is RoomStatus.Expiring -> "삭제 임박"
        }

val RoomStatus.description: String
    get() =
        when (this) {
            is RoomStatus.Shooting -> "${total}장 완성 시 자동으로 3시간 카운트다운이 시작됩니다."
            is RoomStatus.Waiting -> "사진 24장이 모두 모였어요. 공개까지 카운트다운이 진행 중입니다."
            RoomStatus.Opened -> "방이 공개되었어요. 갤러리에서 사진을 확인해보세요."
            is RoomStatus.Expiring -> "방이 곧 자동 삭제됩니다. 갤러리에서 사진을 확인해보세요."
        }

val RoomStatus.primaryButtonText: String
    get() =
        when (this) {
            is RoomStatus.Shooting,
            is RoomStatus.Waiting,
            -> "촬영하기"
            RoomStatus.Opened,
            is RoomStatus.Expiring,
            -> "갤러리 보기"
        }

val RoomStatus.isPrimaryButtonEnabled: Boolean
    get() =
        when (this) {
            is RoomStatus.Waiting -> false
            is RoomStatus.Shooting,
            RoomStatus.Opened,
            is RoomStatus.Expiring,
            -> true
        }
