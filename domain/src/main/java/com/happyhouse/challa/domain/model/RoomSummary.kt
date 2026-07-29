package com.happyhouse.challa.domain.model

data class RoomSummary(
    val id: Long,
    val name: String,
    val remainingCount: Int,
    val totalCount: Int,
)
