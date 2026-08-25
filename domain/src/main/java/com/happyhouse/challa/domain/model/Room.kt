package com.happyhouse.challa.domain.model

data class Room(
    val id: Long,
    val status: RoomStatus,
    val title: String,
    val memberCount: Int,
    val totalPhotoCount: Int,
    val remainedPhotoCount: Int,
    val thumbnailImageUrls: List<String>,
    val photoPrintCompletedAt: String? = null,
    val photoPrintCompletionCheckedAt: String?,
)
