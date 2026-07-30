package com.happyhouse.challa.domain.model

/**
 * 로그인 성공 시 서버가 발급하는 토큰 묶음.
 *
 * @property isNewUser 이번 로그인으로 새로 가입된 유저인지 여부(온보딩 분기용).
 */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val isNewUser: Boolean,
)
