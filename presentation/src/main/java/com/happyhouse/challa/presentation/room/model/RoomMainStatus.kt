package com.happyhouse.challa.presentation.room.model

/**
 * 방 메인 화면에서 표시할 촬영 진행 상태.
 */
enum class RoomMainStatus(
    val label: String,
    val description: String,
    val primaryButtonText: String,
    val isPrimaryButtonEnabled: Boolean,
) {
    /**
     * 아직 24장을 모두 채우지 못해 사용자가 사진을 촬영할 수 있는 상태.
     */
    Shooting(
        label = "촬영중",
        description = "24장 완성 시 자동으로 3시간 카운트다운이 시작됩니다.",
        primaryButtonText = "촬영하기",
        isPrimaryButtonEnabled = true,
    ),

    /**
     * 24장을 모두 채워 공개 전 카운트다운을 기다리는 상태.
     */
    Waiting(
        label = "대기중",
        description = "사진 24장이 모두 모였어요. 공개까지 카운트다운이 진행 중입니다.",
        primaryButtonText = "촬영하기",
        isPrimaryButtonEnabled = false,
    ),

    /**
     * 카운트다운이 끝나 사진 갤러리를 볼 수 있는 상태.
     */
    Published(
        label = "공개됨",
        description = "방이 공개되었어요. 갤러리에서 사진을 확인해보세요.",
        primaryButtonText = "갤러리 보기",
        isPrimaryButtonEnabled = true,
    ),
}
