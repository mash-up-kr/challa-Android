package com.happyhouse.challa.domain.model

import java.time.Instant

/**
 * 방에 찍힌 사진 한 장
 *
 * @param imageUrl 아직 공개되지 않은 사진도 원본 주소를 받는다.
 * @param photographerNickname 촬영자 닉네임. 탈퇴한 유저도 서버가 대체 닉네임을 내려준다.
 * @param photographerProfileImageUrl 촬영자 프로필 사진. 설정하지 않았거나 탈퇴로 삭제됐으면 null
 */
data class Photo(
    val id: Long,
    val imageUrl: String,
    val photographerNickname: String,
    val photographerProfileImageUrl: String?,
    val createdAt: Instant,
)
