package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.model.AuthTokens
import com.happyhouse.challa.domain.result.ChallaResult

interface AuthRepository {
    /**
     * 카카오 로그인으로 발급받은 [idToken] 을 서버로 보내 세션(토큰)을 교환하고, 발급받은 토큰을 저장한다.
     *
     * 카카오 SDK 호출(Activity 필요)은 UI 레이어에서 수행하고, 이 레이어는 ID 토큰만 다룬다.
     */
    suspend fun loginWithKakao(idToken: String): ChallaResult<AuthTokens>

    /**
     * 저장된 로그인 세션(토큰)이 있는지 확인한다.
     *
     * 앱 시작 시 로그인 화면부터 시작할지, 로그인된 화면으로 바로 진입할지 결정하는 데 쓴다.
     */
    suspend fun isLoggedIn(): Boolean
}
