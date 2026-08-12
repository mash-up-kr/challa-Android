package com.happyhouse.challa.domain.model

import java.time.Instant

/**
 * 방에 찍힌 사진 한 장
 *
 * @param imageUrl 아직 공개되지 않은 사진도 원본 주소를 받는다.
 * @param photographerNickname 촬영자 닉네임. 서버가 내려주지 않으면 null
 */
data class Photo(
    val id: Long,
    val imageUrl: String,
    val photographerNickname: String?,
    val photographerProfileImageUrl: String,
    val createdAt: Instant,
)
