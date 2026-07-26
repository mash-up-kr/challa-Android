package com.happyhouse.challa.presentation.camera.model

/** 방의 남은 촬영 수에서 파생되는 촬영 가능 상태입니다. */
enum class RemainingCaptureStatus {
    /** 남은 촬영 수가 없어 촬영할 수 없습니다. */
    UNAVAILABLE,

    /** 남은 촬영 수가 1~5장입니다. */
    LOW,

    /** 남은 촬영 수가 6장 이상입니다. */
    AVAILABLE,
    ;

    val isCaptureAvailable: Boolean
        get() = this != UNAVAILABLE

    companion object {
        fun from(remainingCount: Int): RemainingCaptureStatus =
            when {
                remainingCount <= 0 -> UNAVAILABLE
                remainingCount <= LOW_CAPTURE_COUNT_MAX -> LOW
                else -> AVAILABLE
            }

        private const val LOW_CAPTURE_COUNT_MAX = 5
    }
}

val CameraRoomUiModel.remainingCaptureStatus: RemainingCaptureStatus
    get() = RemainingCaptureStatus.from(remainingCount)
