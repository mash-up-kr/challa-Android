package com.happyhouse.challa.presentation.room.contract

import com.happyhouse.challa.presentation.base.UiState
import com.happyhouse.challa.presentation.room.model.RoomMainStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class RoomMainUiState(
    val title: String = "해피하우스 프작모",
    val memberInitials: ImmutableList<String> = persistentListOf("박", "김", "이"),
    val maxMemberCount: Int = 12,
    val photoCount: Int = 11,
    val totalPhotoCount: Int = 24,
    val isPublished: Boolean = false,
) : UiState {
    val status: RoomMainStatus
        get() =
            when {
                isPublished -> RoomMainStatus.Published
                photoCount >= totalPhotoCount -> RoomMainStatus.Waiting
                else -> RoomMainStatus.Shooting
            }
}
