package com.happyhouse.challa.domain.model

enum class RoomStatus {
    SHOOTING,
    PHOTO_PRINT_PENDING,
    PHOTO_PRINT_COMPLETED,
    UNKNOWN,
    ;

    companion object {
        fun from(value: String): RoomStatus = entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}
