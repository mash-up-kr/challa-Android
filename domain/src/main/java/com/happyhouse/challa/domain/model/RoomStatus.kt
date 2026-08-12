package com.happyhouse.challa.domain.model

/** 방의 진행 상태 */
enum class RoomStatus {
    /** 촬영 중. 아직 필름을 다 채우지 못했다. */
    SHOOTING,

    /** 인화 대기. 필름을 다 채워 인화 완료 시각이 잡혔다. */
    PHOTO_PRINT_PENDING,

    /** 인화 완료. 사진이 공개됐다. */
    PHOTO_PRINT_COMPLETED,

    /**
     * 서버가 상태를 새로 추가했을 때 들어온다.
     * 아는 상태로 넘겨짚으면 실제와 다른 화면을 보여주게 되므로, 화면에서 처리할 수 없는 상태로 다룬다.
     */
    UNKNOWN,
    ;

    companion object {
        fun from(value: String): RoomStatus = entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}
