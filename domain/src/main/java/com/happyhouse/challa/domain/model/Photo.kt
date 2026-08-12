package com.happyhouse.challa.domain.model

/**
 * 방에 찍힌 사진 한 장
 *
 * @param imageUrl 아직 공개되지 않은 사진도 원본 주소를 받는다.
 */
data class Photo(
    val id: Long,
    val imageUrl: String,
)
