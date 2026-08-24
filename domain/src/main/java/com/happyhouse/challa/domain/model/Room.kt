package com.happyhouse.challa.domain.model

import java.time.Instant

/**
 * 방 목록 항목
 *
 * @param photoPrintCompletionCheckedAt 인화 완료를 확인한 시각. 아직 확인하지 않았으면 null.
 *   Boolean이 아니라 시각인 것은 목록 정렬에 쓰기 위해서다.
 */
data class Room(
    val id: Long,
    val status: RoomStatus,
    val title: String,
    val memberCount: Int,
    val totalPhotoCount: Int,
    val remainedPhotoCount: Int,
    val thumbnailImageUrls: List<String>,
    val photoPrintCompletedAt: String? = null,
    val photoPrintCompletionCheckedAt: Instant? = null,
)
