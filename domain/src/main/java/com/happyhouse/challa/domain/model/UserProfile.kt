package com.happyhouse.challa.domain.model

/**
 * 서버에 저장된 유저 프로필.
 *
 * @property nickname 닉네임.
 * @property profileImageUrl 프로필 이미지 URL(미설정 시 null).
 */
data class UserProfile(
    val id: Long,
    val nickname: String,
    val profileImageUrl: String?,
)
