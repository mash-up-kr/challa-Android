package com.happyhouse.challa.domain.model

import java.time.Instant

data class Room(
    val id: Long,
    val status: RoomStatus,
    val title: String,
    val memberCount: Int,
    val totalPhotoCount: Int,
    val remainedPhotoCount: Int,
    val thumbnailImageUrls: List<String>,
    val cover: RoomCover,
    /** 인화가 끝나는 시각. 인화 시각이 아직 잡히지 않았으면 null */
    val photoPrintCompletedAt: Instant? = null,
    val photoPrintCompletionCheckedAt: String?,
)
