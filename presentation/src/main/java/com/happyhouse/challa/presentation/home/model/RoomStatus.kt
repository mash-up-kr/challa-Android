package com.happyhouse.challa.presentation.home.model

import androidx.compose.runtime.Immutable
import kotlin.time.Duration

@Immutable
sealed interface RoomStatus {
    /** 촬영중 — taken/total 장수 표기 */
    @Immutable
    data class Shooting(
        val taken: Int,
        val total: Int,
    ) : RoomStatus

    /** 공개 대기 — D-Day와 남은 시간 표기 */
    @Immutable
    data class Waiting(
        val dDay: Int,
        val remaining: Duration,
    ) : RoomStatus

    /** 공개됨 */
    data object Opened : RoomStatus

    /** 자동 삭제 임박 — D-N 표기 */
    @Immutable
    data class Expiring(
        val dDay: Int,
    ) : RoomStatus
}
