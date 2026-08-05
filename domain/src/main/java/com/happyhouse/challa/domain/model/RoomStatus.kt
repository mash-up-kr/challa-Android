package com.happyhouse.challa.domain.model

/** 방의 진행 상태 */
enum class RoomStatus {
    /** 촬영 중. 아직 필름을 다 채우지 못했다. */
    SHOOTING,

    /** 인화 대기. 필름을 다 채워 인화 완료 시각이 잡혔다. */
    PHOTO_PRINT_PENDING,

    /** 인화 완료. 사진이 공개됐다. */
    PHOTO_PRINT_COMPLETED,
}
