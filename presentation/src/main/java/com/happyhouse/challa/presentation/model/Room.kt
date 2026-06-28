package com.happyhouse.challa.presentation.model

import androidx.compose.runtime.Immutable

const val ROOM_REQUIRED_PHOTO_COUNT = 24

@Immutable
data class Room(
    val id: String,
    val name: String,
    val requiredPhotoCount: Int = ROOM_REQUIRED_PHOTO_COUNT,
    val status: RoomStatus,
)
