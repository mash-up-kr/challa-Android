package com.happyhouse.challa.presentation.home.createroom

/**
 * 방에서 찍을 수 있는 사진 수 옵션. ("얼마나 찍을까요?" 섹션)
 *
 * 방 생성 API 요청에 포함되는 값이며, 첫 번째 옵션([COUNT_24])이 기본 선택값이다.
 */
enum class ShotCount(
    val count: Int,
) {
    COUNT_24(24),
    COUNT_48(48),
    COUNT_72(72),
}
