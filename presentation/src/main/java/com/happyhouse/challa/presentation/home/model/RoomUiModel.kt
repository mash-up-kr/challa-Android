package com.happyhouse.challa.presentation.home.model

import androidx.compose.runtime.Immutable

@Immutable
data class RoomUiModel(
    val id: String,
    val name: String,
    val participantCount: Int,
    val status: HomeRoomStatus,
)
