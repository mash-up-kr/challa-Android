package com.happyhouse.challa.domain.model

import java.time.Instant

/**
 * 방 상세 정보
 *
 * @param totalPhotoCount 필름의 전체 칸 수
 * @param remainedPhotoCount 아직 찍지 않은 칸 수. `totalPhotoCount`에서 빼면 지금까지 찍은 수가 된다.
 * @param photoPrintCompletedAt 인화가 끝나는 시각. 인화 시각이 아직 잡히지 않았으면 null
 */
data class RoomDetail(
    val id: Long,
    val title: String,
    val totalPhotoCount: Int,
    val remainedPhotoCount: Int,
    val invitationCode: String,
    val status: RoomStatus,
    val cover: RoomCover,
    val photoPrintCompletedAt: Instant?,
)
