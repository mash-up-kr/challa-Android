package com.happyhouse.challa.domain.model

data class ShootableRoom(
    val id: Long,
    val title: String,
    val remainingCount: Int,
    val totalCount: Int,
)
