package com.happyhouse.challa.domain.model

import java.time.Instant

/**
 * 방에 찍힌 사진 한 장
 *
 * @param imageUrl 사진 주소. 서버가 내려주지 않으면 null
 * @param photographerNickname 촬영자 닉네임. 서버가 내려주지 않으면 null
 * @param photographerProfileImageUrl 촬영자 프로필 사진. 등록하지 않았으면 null
 * @param createdAt 촬영 시각
 */
data class Photo(
    val id: Long,
    val imageUrl: String?,
    val photographerNickname: String?,
    val photographerProfileImageUrl: String?,
    val createdAt: Instant,
)
