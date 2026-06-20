package com.happyhouse.challa.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class Room(
    val id: String,
    val name: String,
    val status: RoomStatus,
)
